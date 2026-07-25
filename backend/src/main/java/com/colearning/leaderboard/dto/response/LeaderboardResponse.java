package com.colearning.leaderboard.dto.response;

import java.util.List;

/**
 * Leaderboard response containing entries and the current user's rank.
 */
public record LeaderboardResponse(
        String type,           // daily | weekly | alltime
        List<LeaderboardEntryResponse> entries,
        LeaderboardEntryResponse myRank
) {
}
