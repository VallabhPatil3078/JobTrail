package com.jobtrail.backend.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListMessagesResponse;
import com.google.api.services.gmail.model.Message;
import com.jobtrail.backend.model.RawEmail;
import com.jobtrail.backend.model.User;
import com.jobtrail.backend.repository.RawEmailRepository;
import com.jobtrail.backend.repository.UserRepository;
import com.jobtrail.backend.exception.DecryptionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailService {

    private final GoogleAuthorizationCodeFlow flow;
    private final NetHttpTransport httpTransport;
    private final JsonFactory googleJsonFactory;
    private final UserRepository userRepository;
    private final RawEmailRepository rawEmailRepository;
    private final EmailParsingService emailParsingService;
    private final EncryptionService encryptionService;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    @Value("${app.redirect-uri:http://localhost:8080/api/gmail/callback}")
    private String redirectUri;

    private static final String APPLICATION_NAME = "JobTrail";
    private final AtomicBoolean isSyncing = new AtomicBoolean(false);

    public String getAuthorizationUrl(String state) {
        return flow.newAuthorizationUrl()
                .setRedirectUri(redirectUri)
                .setState(state)
                .build();
    }

    @Transactional
    public void exchangeCode(String code, Long userId) {
        try {
            TokenResponse response = flow.newTokenRequest(code)
                    .setRedirectUri(redirectUri)
                    .execute();
            
            String refreshToken = response.getRefreshToken();
            
            User user = userRepository.findById(userId).orElseThrow();
            if (refreshToken != null) {
                user.setEncryptedRefreshToken(encryptionService.encrypt(refreshToken));
                user.setGmailConnectionStatus("CONNECTED");
                userRepository.save(user);
                log.info("Saved encrypted refresh token for user {}", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to exchange auth code", e);
            throw new RuntimeException("Failed to exchange auth code", e);
        }
    }

    @Scheduled(fixedDelay = 900000) // 15 minutes
    public void scheduledSync() {
        if (isSyncing.compareAndSet(false, true)) {
            try {
                executeSync();
            } finally {
                isSyncing.set(false);
            }
        } else {
            log.info("Scheduled sync skipped - sync already in progress.");
        }
    }
    
    public boolean triggerManualSync() {
        if (isSyncing.compareAndSet(false, true)) {
            try {
                executeSync();
                return true;
            } finally {
                isSyncing.set(false);
            }
        }
        return false;
    }

    private void executeSync() {
        log.info("Starting Gmail sync...");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getEncryptedRefreshToken() != null && !user.getEncryptedRefreshToken().isEmpty()) {
                if ("NEEDS_RECONNECT".equals(user.getGmailConnectionStatus())) {
                    log.info("Skipping user {} because they need to reconnect.", user.getEmail());
                    continue;
                }
                try {
                    syncEmailsForUser(user);
                } catch (DecryptionException de) {
                    log.warn("Decryption failed for user {}. Marking as NEEDS_RECONNECT", user.getEmail());
                    user.setGmailConnectionStatus("NEEDS_RECONNECT");
                    userRepository.save(user);
                } catch (Exception e) {
                    log.error("Failed to sync emails for user {}", user.getEmail(), e);
                }
            }
        }
        
        log.info("Finished fetching emails. Triggering email parser...");
        emailParsingService.processUnprocessedEmails();
    }

    private void syncEmailsForUser(User user) throws Exception {
        String decryptedToken = encryptionService.decrypt(user.getEncryptedRefreshToken());
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(googleJsonFactory)
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(decryptedToken);

        Gmail gmail = new Gmail.Builder(httpTransport, googleJsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        String query = "subject:(application OR interview OR offer OR \"thank you for applying\")";
        if (user.getLastSyncedAt() != null) {
            long epochSeconds = user.getLastSyncedAt().atZone(ZoneId.systemDefault()).toEpochSecond();
            query += " after:" + epochSeconds;
        }
        
        ListMessagesResponse response = gmail.users().messages().list("me")
                .setQ(query)
                .setMaxResults(20L)
                .execute();

        List<Message> messages = response.getMessages();
        if (messages == null || messages.isEmpty()) {
            log.info("No new matching emails found.");
            return;
        }

        for (Message msg : messages) {
            if (rawEmailRepository.findByMessageId(msg.getId()).isEmpty()) {
                Message fullMsg = gmail.users().messages().get("me", msg.getId())
                        .setFormat("metadata")
                        .setMetadataHeaders(List.of("Subject", "From", "Date"))
                        .execute();
                        
                RawEmail raw = new RawEmail();
                raw.setMessageId(fullMsg.getId());
                raw.setSnippet(fullMsg.getSnippet());
                
                String dateHeader = null;
                for (var h : fullMsg.getPayload().getHeaders()) {
                    if ("Subject".equalsIgnoreCase(h.getName())) raw.setSubject(h.getValue());
                    if ("From".equalsIgnoreCase(h.getName())) raw.setSender(h.getValue());
                    if ("Date".equalsIgnoreCase(h.getName())) dateHeader = h.getValue();
                }
                
                LocalDateTime emailDate = null;
                if (dateHeader != null) {
                    try {
                        emailDate = ZonedDateTime.parse(dateHeader, DateTimeFormatter.RFC_1123_DATE_TIME).toLocalDateTime();
                    } catch (DateTimeParseException e) {
                        try {
                            emailDate = ZonedDateTime.parse(dateHeader, DateTimeFormatter.RFC_1123_DATE_TIME.withZone(ZoneId.of("UTC"))).toLocalDateTime();
                        } catch (Exception ex) {
                            log.warn("Failed to parse Date header: {}", dateHeader);
                        }
                    }
                }
                
                if (emailDate != null) {
                    raw.setReceivedAt(emailDate);
                } else if (fullMsg.getInternalDate() != null) {
                    raw.setReceivedAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(fullMsg.getInternalDate()), ZoneId.systemDefault()));
                } else {
                    raw.setReceivedAt(LocalDateTime.now());
                }
                
                rawEmailRepository.save(raw);
                log.info("Saved raw email: {}", raw.getSubject());
            }
        }
        user.setLastSyncedAt(LocalDateTime.now());
        userRepository.save(user);
    }
}
