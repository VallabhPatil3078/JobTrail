package com.jobtrail.backend.config;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.gmail.GmailScopes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Configuration
public class GmailConfig {

    @Value("${google.client-id}")
    private String clientId;

    @Value("${google.client-secret}")
    private String clientSecret;

    private static final List<String> SCOPES = Collections.singletonList(GmailScopes.GMAIL_READONLY);
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();

    @Bean
    public NetHttpTransport netHttpTransport() throws GeneralSecurityException, IOException {
        return GoogleNetHttpTransport.newTrustedTransport();
    }

    @Bean
    public JsonFactory googleJsonFactory() {
        return JSON_FACTORY;
    }

    @Bean
    public GoogleAuthorizationCodeFlow googleAuthorizationCodeFlow(NetHttpTransport httpTransport) {
        return new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientId,
                clientSecret,
                SCOPES)
                .setAccessType("offline")
                .setApprovalPrompt("force")
                .build();
    }
}
