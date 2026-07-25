package com.colearning.gamification.internal;

import com.colearning.common.event.DailyCheckinCompletedEvent;
import com.colearning.common.event.FocusSessionFinishedEvent;
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

    /**
     * When a focus session finishes, award EXP (every 10 min = 1 EXP) and check achievements.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFocusSessionFinished(FocusSessionFinishedEvent event) {
        try {
            // Every 10 minutes of focus = 1 EXP (minimum 1)
            int exp = Math.max(1, event.effectiveSeconds() / 600);
            gamificationService.addExp(event.userId(), exp);
            gamificationService.checkAndUnlockAchievements(event.userId());
        } catch (Exception e) {
            log.error("Failed to award EXP for userId={}: {}", event.userId(), e.getMessage(), e);
        }
    }

    /**
     * When a daily check-in is completed, award EXP + tokens and check achievements.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDailyCheckinCompleted(DailyCheckinCompletedEvent event) {
        try {
            gamificationService.addExp(event.userId(), 5);
            gamificationService.addTokens(event.userId(), 3);
            gamificationService.checkAndUnlockAchievements(event.userId());
        } catch (Exception e) {
            log.error("Failed to award check-in bonus for userId={}: {}",
                    event.userId(), e.getMessage(), e);
        }
    }
}
