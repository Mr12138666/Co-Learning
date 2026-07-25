package com.colearning.leaderboard;

import com.colearning.leaderboard.dto.response.LeaderboardResponse;

/**
 * Service for managing study leaderboards using Redis ZSET.
 *
 * <p>Scoring dimensions:
 * <ul>
 *   <li>Focus time (1 sec = 1 point) — primary score</li>
 *   <li>Check-in completion (+50 points per check-in)</li>
 *   <li>Streak bonus (+10 * streakDays per check-in)</li>
 * </ul>
 *
 * <p>Leaderboard types:
 * <ul>
 *   <li>Daily — resets every day</li>
 *   <li>Weekly — resets every week</li>
 *   <li>All-time — cumulative</li>
 * </ul>
 */
public interface LeaderboardService {

    /**
     * Add score to a user across all leaderboards (daily, weekly, all-time).
     */
    void addScore(Long userId, double score);

    /**
     * Get the leaderboard for the specified type.
     *
     * @param type  "daily", "weekly", or "alltime"
     * @param limit max entries to return
     * @param currentUserId the current user's ID (to include their rank)
     */
    LeaderboardResponse getLeaderboard(String type, int limit, Long currentUserId);

    /**
     * Get the current user's rank and score for the specified leaderboard type.
     */
    LeaderboardResponse getMyRank(String type, Long userId);
}
