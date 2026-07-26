package com.colearning.study.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public record CreateTaskRequest(
        Long subjectId,
        Long examGoalId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        LocalDate dueDate,
        LocalDate plannedDate,
        Instant scheduledStart,
        Instant scheduledEnd,
        Integer estimatedMinutes,
        Boolean urgent,
        Boolean important,
        List<Long> tagIds
) {
}
