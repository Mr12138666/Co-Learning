package com.colearning.study.dto.response;

import java.time.Instant;

public record SubjectResponse(
        Long id,
        String name,
        String color,
        Integer sortOrder,
        Instant createdAt,
        Instant updatedAt
) {
}
