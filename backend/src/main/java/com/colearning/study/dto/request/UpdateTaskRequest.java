package com.colearning.study.dto.request;

import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UpdateTaskRequest(
        @Size(max = 200) String title,
        @Size(max = 5000) String description,
        String status,  // TODO | IN_PROGRESS | DONE | ARCHIVED
        LocalDate dueDate,
        Long subjectId,
        Integer sortOrder
) {
}
