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
 *
 * <p>Phase 4 services (LeaderboardEventListener, GamificationEventListener)
 * listen to the same events independently for score updates, EXP gains,
 * and achievement checks.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StudyEventListener {

    private final StatsService statsService;

    /**
     * When a focus session finishes, refresh today's check-in focus total.
     * Leaderboard and gamification updates are handled by their respective listeners.
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
    }

    /**
     * When a daily check-in is completed.
     * Achievement checks and leaderboard streak bonus are handled by their respective listeners.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDailyCheckinCompleted(DailyCheckinCompletedEvent event) {
        log.debug("Processing DailyCheckinCompletedEvent: userId={}, date={}, streak={}",
                event.userId(), event.checkinDate(), event.streakDays());
    }
}
