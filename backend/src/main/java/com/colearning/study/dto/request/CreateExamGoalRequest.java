package com.colearning.study.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record CreateExamGoalRequest(
        @NotBlank @Size(max = 100) String examName,
        @NotNull LocalDate examDate,
        @Size(max = 50) String targetScore
) {
}
