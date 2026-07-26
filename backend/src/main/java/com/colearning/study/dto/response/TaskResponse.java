package com.colearning.study.dto.response;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

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
        LocalDate plannedDate,
        Instant scheduledStart,
        Instant scheduledEnd,
        Integer estimatedMinutes,
        Boolean urgent,
        Boolean important,
        List<TagResponse> tags,
        Long totalFocusSeconds,
        Instant createdAt,
        Instant updatedAt
) {
}
