package com.jobtrail.backend.dto;

import com.jobtrail.backend.model.Application;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public class StatusHistoryDto {

    public record StatusUpdateRequest(
        @NotNull(message = "New status is required") Application.StatusEnum status,
        String note,
        Boolean createReminder
    ) {}

    public record StatusHistoryResponse(
        Long id,
        Long applicationId,
        Application.StatusEnum fromStatus,
        Application.StatusEnum toStatus,
        LocalDateTime changedAt,
        String note,
        Application.DataSourceEnum source
    ) {}
}
