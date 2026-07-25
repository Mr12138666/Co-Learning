package com.colearning.gamification.dto.response;

/**
 * User's gamification profile: experience, level, tokens.
 */
public record GamificationProfileResponse(
        Long userId,
        int totalExp,
        int level,
        int tokens,
        int expToNextLevel,
        int expIntoCurrentLevel
) {
    /**
     * Level formula: level = floor(sqrt(totalExp / 100)) + 1
     * Exp needed for level L: (L-1)^2 * 100
     */
    public static int calculateLevel(int totalExp) {
        return (int) Math.floor(Math.sqrt(totalExp / 100.0)) + 1;
    }

    public static int expForLevel(int level) {
        return (level - 1) * (level - 1) * 100;
    }

    public static GamificationProfileResponse from(Long userId, int totalExp, int tokens) {
        int level = calculateLevel(totalExp);
        int currentLevelExp = expForLevel(level);
        int nextLevelExp = expForLevel(level + 1);
        int expIntoCurrent = totalExp - currentLevelExp;
        int expToNext = nextLevelExp - totalExp;
        return new GamificationProfileResponse(userId, totalExp, level, tokens, expToNext, expIntoCurrent);
    }
}
