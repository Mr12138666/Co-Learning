package com.colearning.study.dto.response;

import java.time.Instant;
import java.time.LocalDate;

public record TaskResponse(
        Long id,
        Long subjectId,
        String subjectName,
        String subjectColor,
        Long examGoalId,
        String title,
        String description,
        String status,
        LocalDate dueDate,
        Integer sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
