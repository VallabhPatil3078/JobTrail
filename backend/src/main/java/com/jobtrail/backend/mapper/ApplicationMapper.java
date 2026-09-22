package com.jobtrail.backend.mapper;

import com.jobtrail.backend.dto.ApplicationDto.ApplicationRequest;
import com.jobtrail.backend.dto.ApplicationDto.ApplicationResponse;
import com.jobtrail.backend.model.Application;
import com.jobtrail.backend.repository.ReminderRepository;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

@Component
public class ApplicationMapper {

    @Autowired
    private ReminderRepository reminderRepository;

    public Application toEntity(ApplicationRequest request) {
        if (request == null) {
            return null;
        }

        Application application = new Application();
        application.setCompany(request.company());
        application.setRole(request.role());
        application.setJobDescription(request.jobDescription());
        application.setJobUrl(request.jobUrl());
        application.setDateApplied(request.dateApplied());
        application.setSource(request.source());
        application.setConfidence(request.confidence());
        // default status is handled by DB / entity if not set, 
        // but we can explicitly set it here for fresh ones if needed.
        application.setStatus(Application.StatusEnum.APPLIED);
        return application;
    }

    public ApplicationResponse toResponse(Application entity) {
        if (entity == null) {
            return null;
        }

        return new ApplicationResponse(
                entity.getId(),
                entity.getCompany(),
                entity.getRole(),
                entity.getJobDescription(),
                entity.getJobUrl(),
                entity.getStatus(),
                entity.getSource(),
                entity.getConfidence(),
                entity.getDateApplied(),
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getId() != null ? reminderRepository.countByApplicationIdAndCompletedAtIsNull(entity.getId()) > 0 : false
        );
    }
}
