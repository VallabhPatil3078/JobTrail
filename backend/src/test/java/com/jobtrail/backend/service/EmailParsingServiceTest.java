package com.jobtrail.backend.service;

import com.jobtrail.backend.model.RawEmail;
import com.jobtrail.backend.model.SuggestedApplication;
import com.jobtrail.backend.repository.RawEmailRepository;
import com.jobtrail.backend.repository.SuggestedApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailParsingServiceTest {

    @Mock
    private RawEmailRepository rawEmailRepository;

    @Mock
    private SuggestedApplicationRepository suggestedApplicationRepository;

    @InjectMocks
    private EmailParsingService emailParsingService;

    @Captor
    private ArgumentCaptor<SuggestedApplication> suggestionCaptor;

    private RawEmail email;

    @BeforeEach
    void setUp() {
        email = new RawEmail();
        email.setId(1L);
        email.setReceivedAt(LocalDateTime.now());
        email.setProcessed(false);
    }

    @Test
    void shouldParseAtsSenderAndExtractRoleAndCompany() {
        email.setSender("no-reply@greenhouse.io");
        email.setSubject("Thank you for applying to Software Engineer at TechCorp");

        when(rawEmailRepository.findByProcessedFalse()).thenReturn(List.of(email));

        emailParsingService.processUnprocessedEmails();

        verify(suggestedApplicationRepository).save(suggestionCaptor.capture());
        SuggestedApplication saved = suggestionCaptor.getValue();

        assertThat(saved.getExtractedCompany()).isEqualTo("TechCorp");
        assertThat(saved.getExtractedRole()).isEqualTo("Software Engineer");
        assertThat(saved.getConfidenceScore()).isEqualTo(90.0); // 40 (ATS) + 50 (Subject)
        assertThat(email.isProcessed()).isTrue();
    }

    @Test
    void shouldParseSubjectPattern2() {
        email.setSender("recruiting@startup.com"); // Not an ATS
        email.setSubject("Applying to Frontend Developer at Cool Startup");

        when(rawEmailRepository.findByProcessedFalse()).thenReturn(List.of(email));

        emailParsingService.processUnprocessedEmails();

        verify(suggestedApplicationRepository).save(suggestionCaptor.capture());
        SuggestedApplication saved = suggestionCaptor.getValue();

        assertThat(saved.getExtractedCompany()).isEqualTo("Cool Startup");
        assertThat(saved.getExtractedRole()).isEqualTo("Frontend Developer");
        assertThat(saved.getConfidenceScore()).isEqualTo(50.0); // Only Subject
    }

    @Test
    void shouldParseSubjectPattern3AndUseSenderFallbackIfOnlyApplicationIsMatched() {
        email.setSender("Acme Corp <careers@acmecorp.com>");
        email.setSubject("Your Application");

        when(rawEmailRepository.findByProcessedFalse()).thenReturn(List.of(email));

        emailParsingService.processUnprocessedEmails();

        verify(suggestedApplicationRepository).save(suggestionCaptor.capture());
        SuggestedApplication saved = suggestionCaptor.getValue();

        assertThat(saved.getExtractedCompany()).isEqualTo("Acme Corp");
        assertThat(saved.getExtractedRole()).isEqualTo("Unknown Role");
        assertThat(saved.getConfidenceScore()).isEqualTo(20.0); // Fallback
    }

    @Test
    void shouldExtractCompanyFromNonAtsDomainAndCleanDisplayName() {
        email.setSender("IBM Talent Acquisition <noreply@ibm.com>");
        email.setSubject("Your Application"); // Subject regex won't match company
        
        when(rawEmailRepository.findByProcessedFalse()).thenReturn(List.of(email));
        emailParsingService.processUnprocessedEmails();
        
        verify(suggestedApplicationRepository).save(suggestionCaptor.capture());
        SuggestedApplication saved = suggestionCaptor.getValue();
        
        assertThat(saved.getExtractedCompany()).isEqualTo("Ibm"); // Domain extraction prioritizes but cleans up. Actually domainToCompanyName("ibm.com") returns "Ibm"
        assertThat(saved.getConfidenceScore()).isEqualTo(40.0);
    }
    
    @Test
    void shouldNotExtractCompanyFromAtsDomain() {
        email.setSender("Greenhouse Mail <noreply@greenhouse-mail.io>");
        email.setSubject("Applying to Software Engineer at Stripe");
        
        when(rawEmailRepository.findByProcessedFalse()).thenReturn(List.of(email));
        emailParsingService.processUnprocessedEmails();
        
        verify(suggestedApplicationRepository).save(suggestionCaptor.capture());
        SuggestedApplication saved = suggestionCaptor.getValue();
        
        assertThat(saved.getExtractedCompany()).isEqualTo("Stripe");
        assertThat(saved.getConfidenceScore()).isEqualTo(90.0); // ATS (40) + Subject (50)
    }

    @Test
    void shouldMarkProcessedButCreateNoSuggestionIfExtractionFails() {
        email.setSender("friend@example.com");
        email.setSubject("Lunch tomorrow?");

        when(rawEmailRepository.findByProcessedFalse()).thenReturn(List.of(email));

        emailParsingService.processUnprocessedEmails();

        verify(suggestedApplicationRepository, never()).save(any());
        assertThat(email.isProcessed()).isTrue();
    }
}
