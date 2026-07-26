package com.colearning.study.dto.response;

import java.time.Instant;

public record TagResponse(
        Long id,
        String name,
        String color,
        Instant createdAt
) {
}
