package com.jobtrail.backend.dto;

import com.jobtrail.backend.model.SuggestedApplication.SuggestionStatusEnum;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SuggestedApplicationDto {
    
    public record RawEmailSummary(
            Long id,
            String subject,
            String sender,
            String snippet,
            LocalDateTime receivedAt
    ) {}
    
    public record SuggestedApplicationResponse(
            Long id,
            RawEmailSummary rawEmail,
            String extractedCompany,
            String extractedRole,
            LocalDate extractedDate,
            Double confidenceScore,
            SuggestionStatusEnum status,
            LocalDateTime createdAt
    ) {}
    
    public record SuggestionConfirmRequest(
            @NotBlank(message = "Company is required") String company,
            String role,
            String jobUrl,
            LocalDate dateApplied,
            Boolean createReminder
    ) {}
}
