package com.colearning.study.dto.response;

import java.time.Instant;

public record FocusSessionResponse(
        Long id,
        Long subjectId,
        Long taskId,
        String status,
        Instant startedAt,
        Instant pausedAt,
        Instant resumedAt,
        Instant endedAt,
        int pausedSeconds,
        int effectiveSeconds,
        int elapsedSeconds,
        Instant createdAt,
        Instant updatedAt
) {
}
