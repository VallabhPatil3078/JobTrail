package com.jobtrail.backend.service;

import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeRequestUrl;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.jobtrail.backend.model.User;
import com.jobtrail.backend.repository.RawEmailRepository;
import com.jobtrail.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GmailServiceTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private GoogleAuthorizationCodeFlow flow;

    @Mock
    private NetHttpTransport httpTransport;

    @Mock
    private JsonFactory googleJsonFactory;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RawEmailRepository rawEmailRepository;

    @Mock
    private EncryptionService encryptionService;

    @Mock
    private EmailParsingService emailParsingService;

    @InjectMocks
    private GmailService gmailService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(gmailService, "clientId", "mock-client-id");
        ReflectionTestUtils.setField(gmailService, "clientSecret", "mock-client-secret");
        ReflectionTestUtils.setField(gmailService, "redirectUri", "http://localhost:8080/api/gmail/callback");
    }

    @Test
    void getAuthorizationUrl_ShouldReturnCorrectUrl() {
        String mockAuthUrl = "https://mock-google-auth.com";
        GoogleAuthorizationCodeRequestUrl requestUrl = mock(GoogleAuthorizationCodeRequestUrl.class);
        
        when(flow.newAuthorizationUrl()).thenReturn(requestUrl);
        when(requestUrl.setRedirectUri(anyString())).thenReturn(requestUrl);
        when(requestUrl.setState(anyString())).thenReturn(requestUrl);
        when(requestUrl.build()).thenReturn(mockAuthUrl);

        String result = gmailService.getAuthorizationUrl("mock-state");

        assertEquals(mockAuthUrl, result);
        verify(flow, times(1)).newAuthorizationUrl();
        verify(requestUrl, times(1)).setRedirectUri("http://localhost:8080/api/gmail/callback");
    }

    @Test
    void exchangeCode_ShouldSaveRefreshToken() throws Exception {
        String mockCode = "test-code";
        Long mockUserId = 1L;
        String mockRefreshToken = "mock-refresh-token";

        User mockUser = new User();
        mockUser.setId(mockUserId);
        mockUser.setEmail("test@example.com");

        GoogleTokenResponse mockTokenResponse = mock(GoogleTokenResponse.class);
        when(mockTokenResponse.getRefreshToken()).thenReturn(mockRefreshToken);

        // Using the deep stub to mock the builder chain
        when(flow.newTokenRequest(mockCode).setRedirectUri(anyString()).execute())
                .thenReturn(mockTokenResponse);

        when(userRepository.findById(mockUserId)).thenReturn(Optional.of(mockUser));
        when(encryptionService.encrypt(mockRefreshToken)).thenReturn("encrypted-token");

        gmailService.exchangeCode(mockCode, mockUserId);

        verify(userRepository, times(1)).findById(mockUserId);
        verify(userRepository, times(1)).save(mockUser);
        assertEquals("encrypted-token", mockUser.getEncryptedRefreshToken());
        assertEquals(User.GmailConnectionStatus.CONNECTED, mockUser.getGmailConnectionStatus());
    }

    @Test
    void exchangeCode_ShouldThrowExceptionOnFailure() throws Exception {
        String mockCode = "invalid-code";
        Long mockUserId = 1L;

        when(flow.newTokenRequest(mockCode).setRedirectUri(anyString()).execute())
                .thenThrow(new RuntimeException("Google API Error"));

        Exception exception = assertThrows(RuntimeException.class, () -> 
            gmailService.exchangeCode(mockCode, mockUserId)
        );

        assertTrue(exception.getMessage().contains("Failed to exchange auth code"));
        verify(userRepository, never()).save(any());
    }

    @Test
    void executeSync_ShouldMarkUserNeedsReconnectOnDecryptionFailure() throws Exception {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");
        mockUser.setEncryptedRefreshToken("encrypted");
        mockUser.setGmailConnectionStatus(User.GmailConnectionStatus.CONNECTED);

        when(userRepository.findAll()).thenReturn(java.util.List.of(mockUser));
        when(encryptionService.decrypt("encrypted")).thenThrow(new com.jobtrail.backend.exception.DecryptionException("Invalid token", new RuntimeException("Simulated exception")));

        ReflectionTestUtils.invokeMethod(gmailService, "executeSync");

        assertEquals(User.GmailConnectionStatus.NEEDS_RECONNECT, mockUser.getGmailConnectionStatus());
        verify(userRepository, times(1)).save(mockUser);
    }
}
