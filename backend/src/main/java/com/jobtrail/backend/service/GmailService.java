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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GmailService {

    private final GoogleAuthorizationCodeFlow flow;
    private final NetHttpTransport httpTransport;
    private final JsonFactory googleJsonFactory;
    private final UserRepository userRepository;
    private final RawEmailRepository rawEmailRepository;

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    private static final String REDIRECT_URI = "http://localhost:8080/api/gmail/callback";
    private static final String APPLICATION_NAME = "JobTrail";

    public String getAuthorizationUrl() {
        return flow.newAuthorizationUrl()
                .setRedirectUri(REDIRECT_URI)
                .build();
    }

    @Transactional
    public void exchangeCode(String code, Long userId) {
        try {
            TokenResponse response = flow.newTokenRequest(code)
                    .setRedirectUri(REDIRECT_URI)
                    .execute();
            
            String refreshToken = response.getRefreshToken();
            
            User user = userRepository.findById(userId).orElseThrow();
            // In a production app, encrypt this token. For local MVP, storing directly.
            if (refreshToken != null) {
                user.setEncryptedRefreshToken(refreshToken);
                userRepository.save(user);
                log.info("Saved refresh token for user {}", user.getEmail());
            }
        } catch (Exception e) {
            log.error("Failed to exchange auth code", e);
            throw new RuntimeException("Failed to exchange auth code", e);
        }
    }

    @Scheduled(fixedDelay = 900000) // 15 minutes
    public void syncEmails() {
        log.info("Starting scheduled Gmail sync...");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            if (user.getEncryptedRefreshToken() != null && !user.getEncryptedRefreshToken().isEmpty()) {
                try {
                    syncEmailsForUser(user);
                } catch (Exception e) {
                    log.error("Failed to sync emails for user {}", user.getEmail(), e);
                }
            }
        }
    }

    private void syncEmailsForUser(User user) throws Exception {
        GoogleCredential credential = new GoogleCredential.Builder()
                .setTransport(httpTransport)
                .setJsonFactory(googleJsonFactory)
                .setClientSecrets(clientId, clientSecret)
                .build()
                .setRefreshToken(user.getEncryptedRefreshToken());

        Gmail gmail = new Gmail.Builder(httpTransport, googleJsonFactory, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

        String query = "subject:(application OR interview OR offer OR \"thank you for applying\")";
        
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
                
                fullMsg.getPayload().getHeaders().forEach(h -> {
                    if ("Subject".equalsIgnoreCase(h.getName())) raw.setSubject(h.getValue());
                    if ("From".equalsIgnoreCase(h.getName())) raw.setSender(h.getValue());
                });
                
                if (fullMsg.getInternalDate() != null) {
                    raw.setReceivedAt(LocalDateTime.ofInstant(Instant.ofEpochMilli(fullMsg.getInternalDate()), ZoneId.systemDefault()));
                } else {
                    raw.setReceivedAt(LocalDateTime.now());
                }
                
                rawEmailRepository.save(raw);
                log.info("Saved raw email: {}", raw.getSubject());
            }
        }
    }
}
