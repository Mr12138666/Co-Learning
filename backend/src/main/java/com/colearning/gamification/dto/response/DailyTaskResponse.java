package com.colearning.gamification.dto.response;

import com.colearning.gamification.internal.entity.DailyTask;

public record DailyTaskResponse(
        Long id,
        String taskType,
        String title,
        String description,
        Integer targetValue,
        Integer currentProgress,
        Integer rewardTokens,
        String status,
        boolean canClaim
) {
    public static DailyTaskResponse from(DailyTask task) {
        return new DailyTaskResponse(
                task.getId(),
                task.getTaskType(),
                task.getTitle(),
                task.getDescription(),
                task.getTargetValue(),
                task.getCurrentProgress(),
                task.getRewardTokens(),
                task.getStatus(),
                task.isCompleted()
        );
    }
}