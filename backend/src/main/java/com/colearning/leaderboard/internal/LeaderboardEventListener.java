package com.colearning.leaderboard.internal;

import com.colearning.common.event.DailyCheckinCompletedEvent;
import com.colearning.common.event.FocusSessionFinishedEvent;
import com.colearning.leaderboard.LeaderboardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listener for leaderboard score updates.
 * Consumes focus session and check-in events to update Redis ZSET scores.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LeaderboardEventListener {

    private final LeaderboardService leaderboardService;

    /**
     * When a focus session finishes, add focus seconds as score.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFocusSessionFinished(FocusSessionFinishedEvent event) {
        try {
            // 1 second of focus = 1 point
            leaderboardService.addScore(event.userId(), event.effectiveSeconds());
        } catch (Exception e) {
            log.error("Failed to update leaderboard for userId={}: {}",
                    event.userId(), e.getMessage(), e);
        }
    }

    /**
     * When a daily check-in is completed, add check-in bonus + streak bonus.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDailyCheckinCompleted(DailyCheckinCompletedEvent event) {
        try {
            // Check-in bonus: 50 + streak * 10
            double bonus = 50.0 + event.streakDays() * 10.0;
            leaderboardService.addScore(event.userId(), bonus);
        } catch (Exception e) {
            log.error("Failed to update leaderboard for checkin userId={}: {}",
                    event.userId(), e.getMessage(), e);
        }
    }
}
