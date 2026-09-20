package com.jobtrail.backend.controller;

import com.jobtrail.backend.dto.ApplicationDto;
import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.model.RawEmail;
import com.jobtrail.backend.model.SuggestedApplication;
import com.jobtrail.backend.repository.SuggestedApplicationRepository;
import com.jobtrail.backend.security.JwtService;
import com.jobtrail.backend.security.UserDetailsServiceImpl;
import com.jobtrail.backend.service.ApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
    value = SuggestionController.class,
    properties = {
        "google.client-id=test-client-id",
        "google.client-secret=test-client-secret",
        "jwt.secret=test-jwt-secret-1234567890"
    }
)
@AutoConfigureMockMvc(addFilters = false)
class SuggestionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SuggestedApplicationRepository suggestionRepository;

    @MockitoBean
    private ApplicationService applicationService;

    @MockitoBean
    private com.jobtrail.backend.repository.ApplicationRepository applicationRepository;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    private SuggestedApplication suggestion;

    @BeforeEach
    void setUp() {
        RawEmail raw = new RawEmail();
        raw.setId(1L);

        suggestion = new SuggestedApplication();
        suggestion.setId(100L);
        suggestion.setRawEmail(raw);
        suggestion.setExtractedCompany("Google");
        suggestion.setExtractedRole("Backend Engineer");
        suggestion.setConfidenceScore(95.0);
        suggestion.setStatus(SuggestedApplication.SuggestionStatusEnum.PENDING);
        suggestion.setExtractedDate(LocalDate.now());
        suggestion.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getPendingSuggestions_ShouldReturnList() throws Exception {
        when(suggestionRepository.findByStatus(SuggestedApplication.SuggestionStatusEnum.PENDING))
                .thenReturn(List.of(suggestion));

        mockMvc.perform(get("/api/suggestions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(100))
                .andExpect(jsonPath("$[0].extractedCompany").value("Google"))
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void confirmSuggestion_ShouldCreateApplicationAndMarkConfirmed() throws Exception {
        when(suggestionRepository.findById(100L)).thenReturn(Optional.of(suggestion));
        
        ApplicationDto.ApplicationResponse mockResponse = new ApplicationDto.ApplicationResponse(
                1L, "Google", "Backend Engineer", null, null,
                Application.StatusEnum.APPLIED, Application.DataSourceEnum.EMAIL_DETECTED,
                95.0, LocalDate.now(), LocalDateTime.now(), LocalDateTime.now()
        );

        when(applicationService.createApplication(any(ApplicationDto.ApplicationRequest.class)))
                .thenReturn(mockResponse);

        String jsonRequest = "{\"company\":\"Google Inc.\", \"role\":\"Senior Backend Engineer\"}";

        mockMvc.perform(post("/api/suggestions/100/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.company").value("Google"));

        // Verify status changed
        assertThat(suggestion.getStatus()).isEqualTo(SuggestedApplication.SuggestionStatusEnum.CONFIRMED);
        verify(suggestionRepository).save(suggestion);

        // Verify that user's edits were used
        ArgumentCaptor<ApplicationDto.ApplicationRequest> appReqCaptor = ArgumentCaptor.forClass(ApplicationDto.ApplicationRequest.class);
        verify(applicationService).createApplication(appReqCaptor.capture());
        
        ApplicationDto.ApplicationRequest captured = appReqCaptor.getValue();
        assertThat(captured.company()).isEqualTo("Google Inc.");
        assertThat(captured.role()).isEqualTo("Senior Backend Engineer");
    }

    @Test
    void rejectSuggestion_ShouldMarkRejected() throws Exception {
        when(suggestionRepository.findById(100L)).thenReturn(Optional.of(suggestion));

        mockMvc.perform(post("/api/suggestions/100/reject"))
                .andExpect(status().isOk());

        assertThat(suggestion.getStatus()).isEqualTo(SuggestedApplication.SuggestionStatusEnum.REJECTED);
        verify(suggestionRepository).save(suggestion);
    }
}
