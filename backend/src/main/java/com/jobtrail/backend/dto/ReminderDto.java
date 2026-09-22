package com.jobtrail.backend.dto;

import java.time.LocalDateTime;

public record ReminderDto(
        Long id,
        Long applicationId,
        String company,
        String role,
        String type,
        LocalDateTime dueDate,
        LocalDateTime createdAt
) {}
