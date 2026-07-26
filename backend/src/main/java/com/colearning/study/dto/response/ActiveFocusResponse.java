package com.colearning.study.dto.response;

import java.time.Instant;

public record ActiveFocusResponse(
        Long sessionId,
        String status,
        Instant startedAt,
        Instant pausedAt,
        Instant resumedAt,
        int pausedSeconds,
        int elapsedSeconds,
        Long subjectId,
        Long taskId,
        Instant graceDeadline,
        String graceReason
) {
}
