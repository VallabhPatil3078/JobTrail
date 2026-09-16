package com.jobtrail.backend.service;

import com.jobtrail.backend.dto.ApplicationDto.ApplicationRequest;
import com.jobtrail.backend.dto.ApplicationDto.ApplicationResponse;
import com.jobtrail.backend.dto.StatusHistoryDto.StatusHistoryResponse;
import com.jobtrail.backend.dto.StatusHistoryDto.StatusUpdateRequest;
import com.jobtrail.backend.exception.IllegalStatusTransitionException;
import com.jobtrail.backend.exception.ResourceNotFoundException;
import com.jobtrail.backend.mapper.ApplicationMapper;
import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.model.Application.StatusEnum;
import com.jobtrail.backend.model.StatusHistory;
import com.jobtrail.backend.repository.ApplicationRepository;
import com.jobtrail.backend.repository.StatusHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final StatusHistoryRepository statusHistoryRepository;
    private final ApplicationMapper applicationMapper;

    // State Machine Validation Map
    private static final Map<StatusEnum, Set<StatusEnum>> VALID_TRANSITIONS = Map.of(
            StatusEnum.APPLIED, Set.of(StatusEnum.OA, StatusEnum.INTERVIEW, StatusEnum.REJECTED, StatusEnum.WITHDRAWN),
            StatusEnum.OA, Set.of(StatusEnum.INTERVIEW, StatusEnum.REJECTED, StatusEnum.WITHDRAWN),
            StatusEnum.INTERVIEW, Set.of(StatusEnum.OFFER, StatusEnum.REJECTED, StatusEnum.WITHDRAWN),
            StatusEnum.OFFER, Set.of(StatusEnum.REJECTED, StatusEnum.WITHDRAWN),
            StatusEnum.REJECTED, Set.of(),
            StatusEnum.WITHDRAWN, Set.of()
    );

    @Transactional(readOnly = true)
    public List<ApplicationResponse> getAllApplications() {
        return applicationRepository.findAll().stream()
                .map(applicationMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationResponse getApplicationById(Long id) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));
        return applicationMapper.toResponse(application);
    }

    @Transactional
    public ApplicationResponse createApplication(ApplicationRequest request) {
        Application application = applicationMapper.toEntity(request);
        Application saved = applicationRepository.save(application);
        return applicationMapper.toResponse(saved);
    }

    @Transactional
    public ApplicationResponse updateApplication(Long id, ApplicationRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        application.setCompany(request.company());
        application.setRole(request.role());
        application.setJobDescription(request.jobDescription());
        application.setJobUrl(request.jobUrl());
        application.setDateApplied(request.dateApplied());
        
        Application saved = applicationRepository.save(application);
        return applicationMapper.toResponse(saved);
    }

    @Transactional
    public void deleteApplication(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }
        applicationRepository.deleteById(id);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long id, StatusUpdateRequest request) {
        Application application = applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found with id: " + id));

        StatusEnum currentStatus = application.getStatus();
        StatusEnum targetStatus = request.status();

        if (currentStatus == targetStatus) {
            return applicationMapper.toResponse(application);
        }

        Set<StatusEnum> allowed = VALID_TRANSITIONS.get(currentStatus);
        if (allowed == null || !allowed.contains(targetStatus)) {
            throw new IllegalStatusTransitionException(
                    String.format("Cannot transition from %s to %s", currentStatus, targetStatus)
            );
        }

        // Record history
        StatusHistory history = new StatusHistory();
        history.setApplication(application);
        history.setFromStatus(currentStatus);
        history.setToStatus(targetStatus);
        history.setNote(request.note());
        history.setSource(Application.DataSourceEnum.MANUAL); // Explicit updates are manual
        statusHistoryRepository.save(history);

        // Update application
        application.setStatus(targetStatus);
        Application saved = applicationRepository.save(application);

        return applicationMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<StatusHistoryResponse> getApplicationHistory(Long id) {
        if (!applicationRepository.existsById(id)) {
            throw new ResourceNotFoundException("Application not found with id: " + id);
        }

        return statusHistoryRepository.findByApplicationIdOrderByChangedAtDesc(id)
                .stream()
                .map(history -> new StatusHistoryResponse(
                        history.getId(),
                        history.getApplication().getId(),
                        history.getFromStatus(),
                        history.getToStatus(),
                        history.getChangedAt(),
                        history.getNote(),
                        history.getSource()
                ))
                .collect(Collectors.toList());
    }
}
