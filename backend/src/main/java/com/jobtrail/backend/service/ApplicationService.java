package com.jobtrail.backend.service;

import com.jobtrail.backend.dto.ApplicationDto.ApplicationRequest;
import com.jobtrail.backend.dto.ApplicationDto.ApplicationResponse;
import com.jobtrail.backend.exception.ResourceNotFoundException;
import com.jobtrail.backend.mapper.ApplicationMapper;
import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final ApplicationMapper applicationMapper;

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
}
