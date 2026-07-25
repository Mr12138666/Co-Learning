package com.colearning.study.internal;

import com.colearning.common.event.DailyCheckinCompletedEvent;
import com.colearning.common.event.FocusSessionFinishedEvent;
import com.colearning.study.StatsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Event listeners for study-related events.
 * Uses @TransactionalEventListener with AFTER_COMMIT phase to ensure
 * the source transaction is committed before processing.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StatsService statsService;

    /**
     * When a focus session finishes, refresh today's check-in focus total.
     * Phase 4 will add leaderboard score update and experience gain here.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFocusSessionFinished(FocusSessionFinishedEvent event) {
        log.debug("Processing FocusSessionFinishedEvent: userId={}, sessionId={}, effectiveSec={}",
                event.userId(), event.sessionId(), event.effectiveSeconds());

        try {
            statsService.refreshTodayFocusTotal(event.userId());
        } catch (Exception e) {
            log.error("Failed to refresh focus total for userId={}: {}",
                    event.userId(), e.getMessage(), e);
        }

        // Phase 4: leaderboard score update
        // Phase 4: experience gain (every 10 min = 1 EXP)
    }

    /**
     * When a daily check-in is completed.
     * Phase 4 will add achievement checks and streak bonus.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDailyCheckinCompleted(DailyCheckinCompletedEvent event) {
        log.debug("Processing DailyCheckinCompletedEvent: userId={}, date={}, streak={}",
                event.userId(), event.checkinDate(), event.streakDays());

        // Phase 4: achievement checks (e.g., "7-day streak", "30-day streak")
        // Phase 4: leaderboard streak bonus
    }
}
