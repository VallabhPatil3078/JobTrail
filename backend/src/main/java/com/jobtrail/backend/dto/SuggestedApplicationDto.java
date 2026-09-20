package com.jobtrail.backend.dto;

import com.jobtrail.backend.model.SuggestedApplication.SuggestionStatusEnum;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class SuggestedApplicationDto {
    
    public record SuggestedApplicationResponse(
            Long id,
            Long rawEmailId,
            String extractedCompany,
            String extractedRole,
            LocalDate extractedDate,
            Double confidenceScore,
            SuggestionStatusEnum status,
            LocalDateTime createdAt
    ) {}
    
    public record SuggestionConfirmRequest(
            String company,
            String role,
            LocalDate dateApplied
    ) {}
}
