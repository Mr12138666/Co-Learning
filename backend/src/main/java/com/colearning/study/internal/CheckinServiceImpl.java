package com.colearning.study.internal;

import com.colearning.common.event.DailyCheckinCompletedEvent;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.study.CheckinService;
import com.colearning.study.StatsService;
import com.colearning.study.dto.request.UpdateCheckinRequest;
import com.colearning.study.dto.response.CheckinResponse;
import com.colearning.study.internal.entity.DailyCheckin;
import com.colearning.study.internal.repository.DailyCheckinRepository;
import com.colearning.study.internal.repository.FocusSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinServiceImpl implements CheckinService {

    private final DailyCheckinRepository dailyCheckinRepository;
    private final FocusSessionRepository focusSessionRepository;
    private final StatsService statsService;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    private static final int STREAK_MIN_MINUTES = 10;

    @Override
    @Transactional
    public CheckinResponse getOrCreateToday(Long userId) {
        LocalDate today = LocalDate.now(clock.getZone());
        DailyCheckin checkin = dailyCheckinRepository
                .findByUserIdAndCheckinDate(userId, today)
                .orElseGet(() -> {
                    DailyCheckin newCheckin = DailyCheckin.builder()
                            .userId(userId)
                            .checkinDate(today)
                            .focusTotalSec(0)
                            .completed(false)
                            .build();
                    // Refresh focus total from sessions
                    refreshFocusTotal(userId, today);
                    return dailyCheckinRepository.save(newCheckin);
                });

        // Always refresh focus total
        statsService.refreshTodayFocusTotal(userId);
        return toResponse(checkin);
    }

    @Override
    @Transactional
    public CheckinResponse updateCheckin(Long userId, UpdateCheckinRequest request) {
        LocalDate date = request.checkinDate() != null
                ? request.checkinDate()
                : LocalDate.now(clock.getZone());

        DailyCheckin checkin = dailyCheckinRepository
                .findByUserIdAndCheckinDate(userId, date)
                .orElseGet(() -> {
                    DailyCheckin newCheckin = DailyCheckin.builder()
                            .userId(userId)
                            .checkinDate(date)
                            .focusTotalSec(0)
                            .completed(false)
                            .build();
                    return dailyCheckinRepository.save(newCheckin);
                });

        if (request.planText() != null) checkin.setPlanText(request.planText());
        if (request.reflectionText() != null) checkin.setReflectionText(request.reflectionText());
        if (request.mood() != null) checkin.setMood(request.mood());
        if (request.images() != null) checkin.setImages(request.images());

        return toResponse(checkin);
    }

    @Override
    @Transactional
    public CheckinResponse completeCheckin(Long userId) {
        LocalDate today = LocalDate.now(clock.getZone());

        DailyCheckin checkin = dailyCheckinRepository
                .findByUserIdAndCheckinDate(userId, today)
                .orElseGet(() -> {
                    DailyCheckin newCheckin = DailyCheckin.builder()
                            .userId(userId)
                            .checkinDate(today)
                            .focusTotalSec(0)
                            .completed(false)
                            .build();
                    return dailyCheckinRepository.save(newCheckin);
                });

        if (checkin.getCompleted()) {
            log.info("Checkin already completed for userId={}, date={}", userId, today);
            return toResponse(checkin);
        }

        // Refresh focus total before completing
        statsService.refreshTodayFocusTotal(userId);
        int todayFocus = focusSessionRepository.sumEffectiveSecondsInRange(
                userId,
                today.atStartOfDay(clock.getZone()).toInstant(),
                today.plusDays(1).atStartOfDay(clock.getZone()).toInstant());
        checkin.setFocusTotalSec(todayFocus);

        checkin.setCompleted(true);

        // Calculate streak
        int streak = calculateStreak(userId, today);

        log.info("Checkin completed: userId={}, date={}, focusSec={}, streak={}",
                userId, today, todayFocus, streak);

        // Publish event
        DailyCheckinCompletedEvent event = new DailyCheckinCompletedEvent(
                checkin.getId(),
                userId,
                today,
                todayFocus,
                streak
        );
        eventPublisher.publishEvent(event);

        return toResponse(checkin);
    }

    @Override
    @Transactional(readOnly = true)
    public CheckinResponse getCheckinByDate(Long userId, LocalDate date) {
        return dailyCheckinRepository.findByUserIdAndCheckinDate(userId, date)
                .map(this::toResponse)
                .orElseThrow(() -> BusinessException.of(ErrorCode.STUDY_SESSION_NOT_FOUND,
                        "该日期无打卡记录: " + date));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CheckinResponse> getHistory(Long userId, LocalDate from, LocalDate to) {
        return dailyCheckinRepository
                .findByUserIdAndCheckinDateBetweenOrderByCheckinDateDesc(userId, from, to)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ===== Private helpers =====

    private void refreshFocusTotal(Long userId, LocalDate date) {
        Instant start = date.atStartOfDay(clock.getZone()).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
        int focus = focusSessionRepository.sumEffectiveSecondsInRange(userId, start, end);

        dailyCheckinRepository.findByUserIdAndCheckinDate(userId, date)
                .ifPresent(c -> c.setFocusTotalSec(focus));
    }

    private int calculateStreak(Long userId, LocalDate today) {
        int streak = 0;
        LocalDate date = today;

        for (int i = 0; i < 365; i++) {
            Instant dayStart = date.atStartOfDay(clock.getZone()).toInstant();
            Instant dayEnd = date.plusDays(1).atStartOfDay(clock.getZone()).toInstant();
            int dayFocus = focusSessionRepository.sumEffectiveSecondsInRange(userId, dayStart, dayEnd);

            boolean hasCompletedCheckin = dailyCheckinRepository
                    .findByUserIdAndCheckinDate(userId, date)
                    .map(DailyCheckin::getCompleted)
                    .orElse(false);

            // A day counts if focus >= 10 min OR completed checkin
            if (dayFocus >= STREAK_MIN_MINUTES * 60 || hasCompletedCheckin) {
                streak++;
                date = date.minusDays(1);
            } else {
                // Allow today to be in-progress
                if (i == 0) {
                    date = date.minusDays(1);
                    continue;
                }
                break;
            }
        }
        return streak;
    }

    private CheckinResponse toResponse(DailyCheckin checkin) {
        return new CheckinResponse(
                checkin.getId(),
                checkin.getCheckinDate(),
                checkin.getPlanText(),
                checkin.getReflectionText(),
                checkin.getMood(),
                checkin.getFocusTotalSec(),
                checkin.getCompleted(),
                checkin.getImages(),
                checkin.getCreatedAt(),
                checkin.getUpdatedAt()
        );
    }
}
