package com.colearning.common.event;

import java.time.LocalDate;

/**
 * Event published when a daily check-in is completed.
 * Consumed by LeaderboardService (streak bonus) and AchievementService.
 */
public record DailyCheckinCompletedEvent(
        Long checkinId,
        Long userId,
        LocalDate checkinDate,
        int focusTotalSec,
        int streakDays
) {
}
