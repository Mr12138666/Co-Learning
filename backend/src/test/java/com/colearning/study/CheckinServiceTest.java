package com.colearning.study;

import com.colearning.common.event.DailyCheckinCompletedEvent;
import com.colearning.study.dto.response.CheckinResponse;
import com.colearning.study.internal.CheckinServiceImpl;
import com.colearning.study.internal.entity.DailyCheckin;
import com.colearning.study.internal.repository.DailyCheckinRepository;
import com.colearning.study.internal.repository.FocusSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckinServiceTest {

    @Mock
    private DailyCheckinRepository dailyCheckinRepository;
    @Mock
    private FocusSessionRepository focusSessionRepository;
    @Mock
    private StatsService statsService;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
    private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
    // CheckinServiceImpl uses LocalDate.now(clock.getZone()) which resolves via system clock,
    // so we must match the actual system date here.
    private final LocalDate today = LocalDate.now(ZoneOffset.UTC);

    private CheckinServiceImpl checkinService;

    @BeforeEach
    void setUp() {
        checkinService = new CheckinServiceImpl(
                dailyCheckinRepository, focusSessionRepository, statsService, fixedClock, eventPublisher);
    }

    private DailyCheckin buildCheckin(Long id, Long userId, LocalDate date, boolean completed) {
        DailyCheckin checkin = DailyCheckin.builder()
                .id(id)
                .userId(userId)
                .checkinDate(date)
                .focusTotalSec(0)
                .completed(completed)
                .build();
        checkin.setCreatedAt(fixedInstant);
        checkin.setUpdatedAt(fixedInstant);
        return checkin;
    }

    // ===== getOrCreateToday() =====

    @Test
    void getOrCreateToday_createsNewRecordWhenNoneExists() {
        Long userId = 1L;

        when(dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today))
                .thenReturn(Optional.empty());
        when(focusSessionRepository.sumEffectiveSecondsInRange(
                eq(userId), any(Instant.class), any(Instant.class)))
                .thenReturn(0);
        DailyCheckin savedCheckin = buildCheckin(1L, userId, today, false);
        when(dailyCheckinRepository.save(any(DailyCheckin.class))).thenReturn(savedCheckin);

        CheckinResponse response = checkinService.getOrCreateToday(userId);

        verify(dailyCheckinRepository).save(any(DailyCheckin.class));
        assertThat(response.checkinDate()).isEqualTo(today);
        assertThat(response.completed()).isFalse();
    }

    @Test
    void getOrCreateToday_returnsExistingRecord() {
        Long userId = 1L;
        DailyCheckin existing = buildCheckin(10L, userId, today, false);
        existing.setPlanText("Study math");

        when(dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today))
                .thenReturn(Optional.of(existing));

        CheckinResponse response = checkinService.getOrCreateToday(userId);

        verify(dailyCheckinRepository, never()).save(any());
        assertThat(response.id()).isEqualTo(10L);
        assertThat(response.planText()).isEqualTo("Study math");
    }

    // ===== completeCheckin() =====

    @Test
    void completeCheckin_isIdempotent() {
        Long userId = 1L;
        DailyCheckin existing = buildCheckin(10L, userId, today, true);

        when(dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today))
                .thenReturn(Optional.of(existing));

        CheckinResponse response = checkinService.completeCheckin(userId);

        verify(eventPublisher, never()).publishEvent(any());
        verify(statsService, never()).refreshTodayFocusTotal(userId);
        assertThat(response.completed()).isTrue();
    }

    @Test
    void completeCheckin_calculatesStreakCorrectly() {
        Long userId = 1L;
        DailyCheckin checkin = buildCheckin(10L, userId, today, false);

        when(dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today))
                .thenReturn(Optional.of(checkin));

        // Today's focus total for the completeCheckin method
        Instant todayStart = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant todayEnd = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        when(focusSessionRepository.sumEffectiveSecondsInRange(userId, todayStart, todayEnd))
                .thenReturn(1200);

        // Yesterday: no focus, no checkin => streak breaks after today
        Instant yesterdayStart = today.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant yesterdayEnd = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        when(focusSessionRepository.sumEffectiveSecondsInRange(userId, yesterdayStart, yesterdayEnd))
                .thenReturn(0);
        when(dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today.minusDays(1)))
                .thenReturn(Optional.empty());

        checkinService.completeCheckin(userId);

        // Streak = 1 (only today)
        ArgumentCaptor<DailyCheckinCompletedEvent> eventCaptor =
                ArgumentCaptor.forClass(DailyCheckinCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().streakDays()).isEqualTo(1);
    }

    @Test
    void completeCheckin_publishesDailyCheckinCompletedEvent() {
        Long userId = 1L;
        DailyCheckin checkin = buildCheckin(10L, userId, today, false);

        when(dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today))
                .thenReturn(Optional.of(checkin));

        Instant todayStart = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant todayEnd = today.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        when(focusSessionRepository.sumEffectiveSecondsInRange(userId, todayStart, todayEnd))
                .thenReturn(1200);

        // Streak: today only (yesterday breaks)
        Instant yesterdayStart = today.minusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant yesterdayEnd = today.atStartOfDay(ZoneOffset.UTC).toInstant();
        when(focusSessionRepository.sumEffectiveSecondsInRange(userId, yesterdayStart, yesterdayEnd))
                .thenReturn(0);
        when(dailyCheckinRepository.findByUserIdAndCheckinDate(userId, today.minusDays(1)))
                .thenReturn(Optional.empty());

        checkinService.completeCheckin(userId);

        ArgumentCaptor<DailyCheckinCompletedEvent> eventCaptor =
                ArgumentCaptor.forClass(DailyCheckinCompletedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        DailyCheckinCompletedEvent event = eventCaptor.getValue();
        assertThat(event.checkinId()).isEqualTo(10L);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.checkinDate()).isEqualTo(today);
        assertThat(event.focusTotalSec()).isEqualTo(1200);
    }
}
