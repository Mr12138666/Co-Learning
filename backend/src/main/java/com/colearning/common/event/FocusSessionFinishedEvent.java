package com.colearning.common.event;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Event published when a focus session is finished.
 * Consumed by StatsService (update daily aggregates),
 * LeaderboardService (refresh score), and ExperienceService (award EXP).
 */
public record FocusSessionFinishedEvent(
        Long sessionId,
        Long userId,
        Long subjectId,
        Long taskId,
        int effectiveSeconds,
        Instant startedAt,
        Instant endedAt,
        LocalDate sessionDate
) {
}
