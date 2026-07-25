package com.colearning.study;

import com.colearning.study.dto.response.StatsResponse;

/**
 * Service for aggregating study statistics (daily/weekly focus, streaks, subject breakdown).
 */
public interface StatsService {

    /**
     * Get comprehensive stats for the current user.
     * Includes today's focus, weekly focus, streak, check-in counts, and daily breakdown.
     */
    StatsResponse getStats(Long userId);

    /**
     * Recalculate and update the focus_total_sec on today's check-in.
     * Called after a focus session is finished.
     */
    void refreshTodayFocusTotal(Long userId);
}
