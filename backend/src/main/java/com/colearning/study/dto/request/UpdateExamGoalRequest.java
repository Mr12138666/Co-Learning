package com.colearning.study.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateExamGoalRequest(
        @Size(max = 100) String examName,
        LocalDate examDate,
        @Size(max = 50) String targetScore,
        String status  // ACTIVE | COMPLETED | ARCHIVED
) {
}
