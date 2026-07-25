package com.colearning.leaderboard.dto.response;

/**
 * A single entry in the leaderboard.
 */
public record LeaderboardEntryResponse(
        Long userId,
        String displayName,
        String avatarUrl,
        int rank,
        double score
) {
}
