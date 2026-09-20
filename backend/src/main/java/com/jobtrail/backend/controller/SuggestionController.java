package com.jobtrail.backend.controller;

import com.jobtrail.backend.dto.ApplicationDto;
import com.jobtrail.backend.dto.SuggestedApplicationDto.SuggestedApplicationResponse;
import com.jobtrail.backend.dto.SuggestedApplicationDto.SuggestionConfirmRequest;
import com.jobtrail.backend.exception.ResourceNotFoundException;
import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.model.SuggestedApplication;
import com.jobtrail.backend.repository.SuggestedApplicationRepository;
import com.jobtrail.backend.service.ApplicationService;
import com.jobtrail.backend.service.SuggestionService;
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

    private final SuggestionService suggestionService;

    @GetMapping
    public ResponseEntity<List<SuggestedApplicationResponse>> getPendingSuggestions() {
        return ResponseEntity.ok(suggestionService.getPendingSuggestions());
    }

    @PostMapping("/{id}/confirm")
    public ResponseEntity<ApplicationDto.ApplicationResponse> confirmSuggestion(
            @PathVariable Long id,
            @RequestBody SuggestionConfirmRequest request) {
        return ResponseEntity.ok(suggestionService.confirmSuggestion(id, request));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> rejectSuggestion(@PathVariable Long id) {
        suggestionService.rejectSuggestion(id);
        return ResponseEntity.ok().build();
    }
}
