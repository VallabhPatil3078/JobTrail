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
    void connect_ShouldRedirectToGoogleAuthUrl() throws Exception {
        String mockUrl = "https://accounts.google.com/o/oauth2/auth?mock=true";
        when(gmailService.getAuthorizationUrl()).thenReturn(mockUrl);

        mockMvc.perform(get("/api/gmail/connect"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl(mockUrl));
    }

    @Test
    void callback_ShouldExchangeCodeWhenUserExists() throws Exception {
        String mockCode = "mock_auth_code_123";
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        when(userRepository.findAll()).thenReturn(List.of(mockUser));

        mockMvc.perform(get("/api/gmail/callback").param("code", mockCode))
                .andExpect(status().isOk())
                .andExpect(content().string("Gmail successfully connected! You can now close this tab."));

        verify(gmailService, times(1)).exchangeCode(mockCode, 1L);
    }

    @Test
    void callback_ShouldThrowExceptionWhenNoUserExists() throws Exception {
        String mockCode = "mock_auth_code_123";

        when(userRepository.findAll()).thenReturn(List.of());

        // Spring MVC will wrap runtime exceptions in a 500 error if there's no custom handler
        org.assertj.core.api.Assertions.assertThatThrownBy(() ->
                mockMvc.perform(get("/api/gmail/callback").param("code", mockCode))
        ).hasCauseInstanceOf(RuntimeException.class)
         .hasMessageContaining("No user found in DB!");
    }

    @Test
    void syncNow_ShouldCallGmailService() throws Exception {
        mockMvc.perform(post("/api/gmail/sync"))
                .andExpect(status().isOk());

        verify(gmailService, times(1)).syncEmails();
    }
}
