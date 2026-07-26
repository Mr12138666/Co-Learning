package com.colearning.gamification.internal;

import com.colearning.common.event.DailyCheckinCompletedEvent;
import com.colearning.common.event.FocusSessionFinishedEvent;
import com.colearning.gamification.DailyTaskService;
import com.colearning.gamification.GamificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for gamification: awards EXP and tokens, checks achievements.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class GamificationEventListener {

    private final GamificationService gamificationService;
    private final DailyTaskService dailyTaskService;

    /**
     * When a focus session finishes, award EXP (every 10 min = 1 EXP) and tokens (every 30 min = 1 token),
     * then check achievements.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFocusSessionFinished(FocusSessionFinishedEvent event) {
        try {
            // Minimum 60 seconds of effective focus to earn rewards
            if (event.effectiveSeconds() < 60) {
                log.info("Session too short for rewards: userId={}, effectiveSec={}",
                        event.userId(), event.effectiveSeconds());
                return;
            }

            // Every 10 minutes of focus = 1 EXP
            int exp = event.effectiveSeconds() / 600;
            if (exp > 0) {
                gamificationService.addExp(event.userId(), exp);
            }

            // Every 30 minutes of focus = 1 token
            int tokens = event.effectiveSeconds() / 1800;
            if (tokens > 0) {
                gamificationService.addTokens(event.userId(), tokens);
            }

            gamificationService.checkAndUnlockAchievements(event.userId());
        } catch (Exception e) {
            log.error("Failed to award EXP/tokens for userId={}: {}", event.userId(), e.getMessage(), e);
        }
    }

    /**
     * When a daily check-in is completed, award EXP + tokens and check achievements.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDailyCheckinCompleted(DailyCheckinCompletedEvent event) {
        try {
            gamificationService.addExp(event.userId(), 5);
            // Base 5 tokens + streak bonus (1 per day)
            gamificationService.addTokens(event.userId(), 5 + event.streakDays());
            gamificationService.checkAndUnlockAchievements(event.userId());
            
            // Update daily task progress
            dailyTaskService.onCheckinCompleted(event.userId());
        } catch (Exception e) {
            log.error("Failed to award check-in bonus for userId={}: {}",
                    event.userId(), e.getMessage(), e);
        }
    }
}
