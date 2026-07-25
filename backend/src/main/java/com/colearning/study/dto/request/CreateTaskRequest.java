package com.colearning.study.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateTaskRequest(
        Long subjectId,
        Long examGoalId,
        @NotBlank @Size(max = 200) String title,
        @Size(max = 5000) String description,
        LocalDate dueDate
) {
}
