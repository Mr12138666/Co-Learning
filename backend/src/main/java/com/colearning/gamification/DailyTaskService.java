package com.colearning.gamification;

import com.colearning.gamification.dto.response.DailyTaskResponse;
import java.util.List;

public interface DailyTaskService {

    /**
     * Get today's tasks for a user.
     * Creates tasks if they don't exist for today.
     */
    List<DailyTaskResponse> getTodayTasks(Long userId);

    /**
     * Claim rewards for a completed task.
     */
    DailyTaskResponse claimReward(Long userId, Long taskId);

    /**
     * Update task progress when focus session finishes.
     */
    void onFocusSessionFinished(Long userId, int effectiveSeconds);

    /**
     * Update task progress when user feeds pet.
     */
    void onFeedPet(Long userId);

    /**
     * Update task progress when user completes checkin.
     */
    void onCheckinCompleted(Long userId);

    /**
     * Update task progress when user writes a journal.
     */
    void onWriteJournal(Long userId);

    /**
     * Generate daily tasks for all users.
     * Called by scheduler at midnight.
     */
    void generateDailyTasks();

    /**
     * Clean up old tasks (older than 7 days).
     */
    void cleanupOldTasks();
}