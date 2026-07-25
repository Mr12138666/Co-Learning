package com.colearning.gamification.dto.response;

import com.colearning.gamification.internal.entity.Achievement;
import java.time.Instant;

/**
 * Achievement response with unlock status.
 */
public record AchievementResponse(
        Long id,
        String code,
        String name,
        String description,
        String category,
        String conditionType,
        int conditionValue,
        String icon,
        int expReward,
        int tokenReward,
        boolean unlocked,
        Instant unlockedAt
) {
    public static AchievementResponse from(Achievement ach, boolean unlocked, Instant unlockedAt) {
        return new AchievementResponse(
                ach.getId(),
                ach.getCode(),
                ach.getName(),
                ach.getDescription(),
                ach.getCategory(),
                ach.getConditionType(),
                ach.getConditionValue(),
                ach.getIcon(),
                ach.getExpReward(),
                ach.getTokenReward(),
                unlocked,
                unlockedAt
        );
    }
}
