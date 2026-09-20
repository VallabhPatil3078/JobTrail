package com.jobtrail.backend.controller;

import com.jobtrail.backend.dto.ApplicationDto;
import com.jobtrail.backend.dto.SuggestedApplicationDto.SuggestedApplicationResponse;
import com.jobtrail.backend.dto.SuggestedApplicationDto.SuggestionConfirmRequest;
import com.jobtrail.backend.exception.ResourceNotFoundException;
import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.model.SuggestedApplication;
import com.jobtrail.backend.repository.SuggestedApplicationRepository;
import com.jobtrail.backend.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestedApplicationRepository suggestionRepository;
    private final ApplicationService applicationService;
    private final com.jobtrail.backend.repository.ApplicationRepository applicationRepository;

    @GetMapping
    public ResponseEntity<List<SuggestedApplicationResponse>> getPendingSuggestions() {
        List<SuggestedApplicationResponse> responses = suggestionRepository.findByStatus(SuggestedApplication.SuggestionStatusEnum.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/{id}/confirm")
    @Transactional
    public ResponseEntity<ApplicationDto.ApplicationResponse> confirmSuggestion(
            @PathVariable Long id,
            @RequestBody SuggestionConfirmRequest request) {
        
        SuggestedApplication suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found with id: " + id));
        
        if (suggestion.getStatus() != SuggestedApplication.SuggestionStatusEnum.PENDING) {
            throw new IllegalStateException("Can only confirm PENDING suggestions.");
        }
        
        String company = request.company() != null ? request.company() : suggestion.getExtractedCompany();
        String role = request.role() != null ? request.role() : suggestion.getExtractedRole();
        java.time.LocalDate dateApplied = request.dateApplied() != null ? request.dateApplied() : suggestion.getExtractedDate();

        // 1. Deduplication Check (3-day window)
        List<Application> existingApps = applicationRepository.findByCompanyIgnoreCase(company);
        for (Application app : existingApps) {
            if (app.getRole() != null && app.getRole().equalsIgnoreCase(role)) {
                if (app.getDateApplied() != null && dateApplied != null) {
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(app.getDateApplied(), dateApplied);
                    if (Math.abs(daysBetween) <= 3) {
                        throw new com.jobtrail.backend.exception.DuplicateApplicationException("A similar application for this role already exists within 3 days.");
                    }
                } else if (app.getDateApplied() == null && dateApplied == null) {
                    throw new com.jobtrail.backend.exception.DuplicateApplicationException("An application for this role already exists.");
                }
            }
        }

        // 2. Create the actual application based on user's final edits
        ApplicationDto.ApplicationRequest appRequest = new ApplicationDto.ApplicationRequest(
                company,
                role,
                null, // jobDescription
                null, // jobUrl
                dateApplied,
                Application.DataSourceEnum.EMAIL_DETECTED,
                suggestion.getConfidenceScore()
        );

        ApplicationDto.ApplicationResponse application = applicationService.createApplication(appRequest);

        // 2. Mark suggestion as confirmed
        suggestion.setStatus(SuggestedApplication.SuggestionStatusEnum.CONFIRMED);
        suggestionRepository.save(suggestion);

        return ResponseEntity.ok(application);
    }

    @PostMapping("/{id}/reject")
    @Transactional
    public ResponseEntity<Void> rejectSuggestion(@PathVariable Long id) {
        SuggestedApplication suggestion = suggestionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Suggestion not found with id: " + id));
        
        if (suggestion.getStatus() != SuggestedApplication.SuggestionStatusEnum.PENDING) {
            throw new IllegalStateException("Can only reject PENDING suggestions.");
        }

        suggestion.setStatus(SuggestedApplication.SuggestionStatusEnum.REJECTED);
        suggestionRepository.save(suggestion);

        return ResponseEntity.ok().build();
    }

    private SuggestedApplicationResponse mapToResponse(SuggestedApplication entity) {
        return new SuggestedApplicationResponse(
                entity.getId(),
                entity.getRawEmail().getId(),
                entity.getExtractedCompany(),
                entity.getExtractedRole(),
                entity.getExtractedDate(),
                entity.getConfidenceScore(),
                entity.getStatus(),
                entity.getCreatedAt()
        );
    }
}
