package com.jobtrail.backend.controller;

import com.jobtrail.backend.dto.ApplicationDto.ApplicationRequest;
import com.jobtrail.backend.dto.ApplicationDto.ApplicationResponse;
import com.jobtrail.backend.service.ApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
public class ApplicationController {

    private final ApplicationService applicationService;

    @GetMapping
    public ResponseEntity<List<ApplicationResponse>> getAllApplications() {
        return ResponseEntity.ok(applicationService.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationResponse> getApplicationById(@PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationById(id));
    }

    @PostMapping
    public ResponseEntity<ApplicationResponse> createApplication(@Valid @RequestBody ApplicationRequest request) {
        ApplicationResponse created = applicationService.createApplication(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApplicationResponse> updateApplication(
            @PathVariable Long id,
            @Valid @RequestBody ApplicationRequest request) {
        return ResponseEntity.ok(applicationService.updateApplication(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        applicationService.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApplicationResponse> updateApplicationStatus(
            @PathVariable Long id,
            @Valid @RequestBody com.jobtrail.backend.dto.StatusHistoryDto.StatusUpdateRequest request) {
        return ResponseEntity.ok(applicationService.updateApplicationStatus(id, request));
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<com.jobtrail.backend.dto.StatusHistoryDto.StatusHistoryResponse>> getApplicationHistory(
            @PathVariable Long id) {
        return ResponseEntity.ok(applicationService.getApplicationHistory(id));
    }
}
