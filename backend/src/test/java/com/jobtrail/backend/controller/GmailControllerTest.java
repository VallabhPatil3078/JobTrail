package com.jobtrail.backend.controller;

import com.jobtrail.backend.model.User;
import com.jobtrail.backend.repository.UserRepository;
import com.jobtrail.backend.service.GmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import com.jobtrail.backend.security.JwtService;
import com.jobtrail.backend.security.UserDetailsServiceImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import java.util.Optional;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    value = GmailController.class,
    properties = {
        "google.client-id=test-client-id",
        "google.client-secret=test-client-secret",
        "jwt.secret=test-jwt-secret-1234567890"
    }
)
@AutoConfigureMockMvc(addFilters = false) // Bypass Spring Security for controller unit tests
class GmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GmailService gmailService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @Test
    @WithMockUser(username = "test@example.com")
    void connect_ShouldReturnAuthUrl() throws Exception {
        String mockAuthUrl = "https://mock-google-auth.com";
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(mockUser));
        when(jwtService.generateStateToken(1L)).thenReturn("mock-state");
        when(gmailService.getAuthorizationUrl("mock-state")).thenReturn(mockAuthUrl);

        mockMvc.perform(get("/api/gmail/auth-url"))
                .andExpect(status().isOk())
                .andExpect(content().json("{\"url\":\"" + mockAuthUrl + "\"}"));
    }

    @Test
    void callback_ShouldExchangeCodeAndRedirect() throws Exception {
        String mockCode = "mock_auth_code_123";
        String mockState = "mock_state_token";

        when(jwtService.extractUserIdFromStateToken(mockState)).thenReturn(1L);

        mockMvc.perform(get("/api/gmail/callback").param("code", mockCode).param("state", mockState))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost:5173/settings?gmail_connected=true"));

        verify(gmailService, times(1)).exchangeCode(mockCode, 1L);
    }

    @Test
    void syncNow_ShouldTriggerSyncAndReturnOk() throws Exception {
        when(gmailService.triggerManualSync()).thenReturn(true);
        mockMvc.perform(post("/api/gmail/sync"))
                .andExpect(status().isOk());
    }
}
