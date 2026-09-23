package com.jobtrail.backend.service;

import com.jobtrail.backend.dto.ApplicationDto;
import com.jobtrail.backend.dto.SuggestedApplicationDto.SuggestionConfirmRequest;
import com.jobtrail.backend.exception.DuplicateApplicationException;
import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.model.SuggestedApplication;
import com.jobtrail.backend.repository.ApplicationRepository;
import com.jobtrail.backend.repository.SuggestedApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SuggestionServiceTest {

    @Mock
    private SuggestedApplicationRepository suggestionRepository;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private SuggestionService suggestionService;

    private SuggestedApplication pendingSuggestion;
    private Application existingApp;

    @BeforeEach
    void setUp() {
        pendingSuggestion = new SuggestedApplication();
        pendingSuggestion.setId(1L);
        pendingSuggestion.setExtractedCompany("IBM");
        pendingSuggestion.setExtractedRole("Software Engineer");
        pendingSuggestion.setStatus(SuggestedApplication.SuggestionStatusEnum.PENDING);
        pendingSuggestion.setExtractedDate(LocalDate.now());
        
        existingApp = new Application();
        existingApp.setId(10L);
        existingApp.setCompany("IBM");
        existingApp.setRole("Software Engineer");
        existingApp.setDateApplied(LocalDate.now());
    }

    @Test
    void confirmSuggestion_ThrowsConflict_WhenJobUrlsAreNull() {
        when(suggestionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pendingSuggestion));
        when(applicationRepository.findByCompanyIgnoreCase("IBM")).thenReturn(List.of(existingApp));

        SuggestionConfirmRequest request = new SuggestionConfirmRequest("IBM", "Software Engineer", null, LocalDate.now(), false);

        assertThrows(DuplicateApplicationException.class, () -> suggestionService.confirmSuggestion(1L, request));
    }

    @Test
    void confirmSuggestion_ThrowsConflict_WhenJobUrlsAreIdentical() {
        existingApp.setJobUrl("https://ibm.com/jobs/123");
        
        when(suggestionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pendingSuggestion));
        when(applicationRepository.findByCompanyIgnoreCase("IBM")).thenReturn(List.of(existingApp));

        SuggestionConfirmRequest request = new SuggestionConfirmRequest("IBM", "Software Engineer", "https://ibm.com/jobs/123", LocalDate.now(), false);

        assertThrows(DuplicateApplicationException.class, () -> suggestionService.confirmSuggestion(1L, request));
    }

    @Test
    void confirmSuggestion_Allows_WhenJobUrlsDiffer() {
        existingApp.setJobUrl("https://ibm.com/jobs/123");
        
        when(suggestionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pendingSuggestion));
        when(applicationRepository.findByCompanyIgnoreCase("IBM")).thenReturn(List.of(existingApp));
        when(applicationService.createApplication(any())).thenReturn(new ApplicationDto.ApplicationResponse(1L, "IBM", "Software Engineer", null, "https://ibm.com/jobs/456", Application.StatusEnum.APPLIED, Application.DataSourceEnum.EMAIL_DETECTED, 90.0, LocalDate.now(), null, null, false));

        SuggestionConfirmRequest request = new SuggestionConfirmRequest("IBM", "Software Engineer", "https://ibm.com/jobs/456", LocalDate.now(), false);

        assertDoesNotThrow(() -> suggestionService.confirmSuggestion(1L, request));
        verify(applicationService).createApplication(any());
    }

    @Test
    void confirmSuggestion_ThrowsConflict_WhenExistingHasUrlButNewDoesNot() {
        existingApp.setJobUrl("https://ibm.com/jobs/123");
        
        when(suggestionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pendingSuggestion));
        when(applicationRepository.findByCompanyIgnoreCase("IBM")).thenReturn(List.of(existingApp));

        SuggestionConfirmRequest request = new SuggestionConfirmRequest("IBM", "Software Engineer", null, LocalDate.now(), false);

        assertThrows(DuplicateApplicationException.class, () -> suggestionService.confirmSuggestion(1L, request));
    }
    
    @Test
    void confirmSuggestion_ThrowsConflict_WhenNewHasUrlButExistingDoesNot() {
        // existingApp has no jobUrl
        
        when(suggestionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pendingSuggestion));
        when(applicationRepository.findByCompanyIgnoreCase("IBM")).thenReturn(List.of(existingApp));

        SuggestionConfirmRequest request = new SuggestionConfirmRequest("IBM", "Software Engineer", "https://ibm.com/jobs/123", LocalDate.now(), false);

        assertThrows(DuplicateApplicationException.class, () -> suggestionService.confirmSuggestion(1L, request));
    }
}
