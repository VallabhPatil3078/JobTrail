package com.jobtrail.backend.dto;

import com.jobtrail.backend.model.Application;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ApplicationDto {

    public record ApplicationRequest(
        @NotBlank(message = "Company is required") String company,
        @NotBlank(message = "Role is required") String role,
        String jobDescription,
        String jobUrl,
        LocalDate dateApplied,
        @NotNull(message = "Source is required") Application.DataSourceEnum source,
        Double confidence,
        Long sourceRawEmailId
    ) {}

    public record ApplicationResponse(
        Long id,
        String company,
        String role,
        String jobDescription,
        String jobUrl,
        Application.StatusEnum status,
        Application.DataSourceEnum source,
        Double confidence,
        LocalDate dateApplied,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {}
}
