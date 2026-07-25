package com.colearning.study.dto.response;

import java.time.LocalDate;
import java.time.Instant;

public record ExamGoalResponse(
        Long id,
        String examName,
        LocalDate examDate,
        String targetScore,
        String status,
        long daysRemaining,
        Instant createdAt,
        Instant updatedAt
) {
}
