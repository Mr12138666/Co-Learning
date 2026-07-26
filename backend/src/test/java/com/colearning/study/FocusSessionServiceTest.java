package com.colearning.study;

import com.colearning.common.event.FocusSessionFinishedEvent;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.gamification.DailyTaskService;
import com.colearning.study.dto.request.StartFocusRequest;
import com.colearning.study.dto.response.FocusSessionResponse;
import com.colearning.study.internal.FocusSessionServiceImpl;
import com.colearning.study.internal.entity.FocusSession;
import com.colearning.study.internal.repository.FocusSessionRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FocusSessionServiceTest {

    @Mock
    private FocusSessionRepository focusSessionRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private DailyTaskService dailyTaskService;

    private final Instant fixedInstant = Instant.parse("2025-01-15T10:00:00Z");
    private final Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);

    private FocusSessionServiceImpl focusSessionService;

    @BeforeEach
    void setUp() {
        focusSessionService = new FocusSessionServiceImpl(
                focusSessionRepository, fixedClock, eventPublisher, dailyTaskService);
        ReflectionTestUtils.setField(focusSessionService, "maxSessionHours", 8);
        ReflectionTestUtils.setField(focusSessionService, "maxPauseHours", 1);
        ReflectionTestUtils.setField(focusSessionService, "gracePeriodMinutes", 30);
    }

    private FocusSession buildSession(Long id, Long userId, String status, Instant startedAt) {
        FocusSession session = FocusSession.builder()
                .id(id)
                .userId(userId)
                .subjectId(10L)
                .taskId(20L)
                .status(status)
                .startedAt(startedAt)
                .pausedSeconds(0)
                .effectiveSeconds(0)
                .build();
        session.setCreatedAt(startedAt);
        session.setUpdatedAt(fixedInstant);
        return session;
    }

    // ===== start() =====

    @Test
    void start_withSameClientRequestId_returnsExistingSession() {
        Long userId = 1L;
        StartFocusRequest request = new StartFocusRequest(10L, 20L, "req-001");
        FocusSession existing = buildSession(100L, userId, "ACTIVE", fixedInstant.minusSeconds(600));
        existing.setClientRequestId("req-001");

        when(focusSessionRepository.findByUserIdAndClientRequestId(userId, "req-001"))
                .thenReturn(Optional.of(existing));

        FocusSessionResponse response = focusSessionService.start(userId, request);

        assertThat(response.id()).isEqualTo(100L);
        verify(focusSessionRepository, never()).save(any());
    }

    @Test
    void start_whenActiveSessionExists_throwsException() {
        Long userId = 1L;
        StartFocusRequest request = new StartFocusRequest(10L, 20L, "req-002");
        FocusSession activeSession = buildSession(50L, userId, "ACTIVE", fixedInstant.minusSeconds(300));

        when(focusSessionRepository.findByUserIdAndClientRequestId(userId, "req-002"))
                .thenReturn(Optional.empty());
        when(focusSessionRepository.findByUserIdAndStatusIn(eq(userId), anyList()))
                .thenReturn(Optional.of(activeSession));

        assertThatThrownBy(() -> focusSessionService.start(userId, request))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.STUDY_SESSION_ALREADY_ACTIVE));
    }

    // ===== pause() =====

    @Test
    void pause_fromActiveState_succeeds() {
        Long userId = 1L;
        Long sessionId = 100L;
        FocusSession session = buildSession(sessionId, userId, "ACTIVE", fixedInstant.minusSeconds(1800));

        when(focusSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        FocusSessionResponse response = focusSessionService.pause(userId, sessionId);

        assertThat(response.status()).isEqualTo("PAUSED");
        assertThat(session.getPausedAt()).isEqualTo(fixedInstant);
    }

    @Test
    void pause_fromNonActiveState_throwsException() {
        Long userId = 1L;
        Long sessionId = 100L;
        FocusSession session = buildSession(sessionId, userId, "PAUSED", fixedInstant.minusSeconds(3600));
        session.setPausedAt(fixedInstant.minusSeconds(1800));

        when(focusSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> focusSessionService.pause(userId, sessionId))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.STUDY_INVALID_STATE_TRANSITION));
    }

    // ===== resume() =====

    @Test
    void resume_fromPausedState_accumulatesPausedSeconds() {
        Long userId = 1L;
        Long sessionId = 100L;
        FocusSession session = buildSession(sessionId, userId, "PAUSED", fixedInstant.minusSeconds(3600));
        session.setPausedSeconds(600);
        session.setPausedAt(fixedInstant.minusSeconds(1800)); // paused 30 min ago

        when(focusSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        FocusSessionResponse response = focusSessionService.resume(userId, sessionId);

        assertThat(response.status()).isEqualTo("ACTIVE");
        assertThat(session.getPausedSeconds()).isEqualTo(600 + 1800);
        assertThat(session.getPausedAt()).isNull();
        assertThat(session.getResumedAt()).isEqualTo(fixedInstant);
    }

    // ===== finish() =====

    @Test
    void finish_calculatesEffectiveSeconds() {
        Long userId = 1L;
        Long sessionId = 100L;
        // Started 1 hour ago, 600s paused => effective = 3600 - 600 = 3000
        FocusSession session = buildSession(sessionId, userId, "ACTIVE", fixedInstant.minusSeconds(3600));
        session.setPausedSeconds(600);

        when(focusSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        FocusSessionResponse response = focusSessionService.finish(userId, sessionId);

        assertThat(response.status()).isEqualTo("FINISHED");
        assertThat(response.effectiveSeconds()).isEqualTo(3000);
    }

    @Test
    void finish_publishesFocusSessionFinishedEvent() {
        Long userId = 1L;
        Long sessionId = 100L;
        FocusSession session = buildSession(sessionId, userId, "ACTIVE", fixedInstant.minusSeconds(3600));
        session.setPausedSeconds(0);

        when(focusSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        focusSessionService.finish(userId, sessionId);

        ArgumentCaptor<FocusSessionFinishedEvent> eventCaptor =
                ArgumentCaptor.forClass(FocusSessionFinishedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        FocusSessionFinishedEvent event = eventCaptor.getValue();
        assertThat(event.sessionId()).isEqualTo(sessionId);
        assertThat(event.userId()).isEqualTo(userId);
        assertThat(event.effectiveSeconds()).isEqualTo(3600);
        assertThat(event.startedAt()).isEqualTo(fixedInstant.minusSeconds(3600));
        assertThat(event.endedAt()).isEqualTo(fixedInstant);
    }

    // ===== abort() =====

    @Test
    void abort_setsEffectiveSecondsToZero() {
        Long userId = 1L;
        Long sessionId = 100L;
        FocusSession session = buildSession(sessionId, userId, "ACTIVE", fixedInstant.minusSeconds(3600));
        session.setPausedSeconds(600);

        when(focusSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        FocusSessionResponse response = focusSessionService.abort(userId, sessionId);

        assertThat(response.status()).isEqualTo("ABORTED");
        assertThat(response.effectiveSeconds()).isEqualTo(0);
    }

    // ===== getSession() =====

    @Test
    void getSession_whenUserIdDoesNotMatch_throwsException() {
        Long ownerId = 1L;
        Long otherUserId = 2L;
        Long sessionId = 100L;
        FocusSession session = buildSession(sessionId, ownerId, "ACTIVE", fixedInstant.minusSeconds(600));

        when(focusSessionRepository.findById(sessionId)).thenReturn(Optional.of(session));

        assertThatThrownBy(() -> focusSessionService.getSession(otherUserId, sessionId))
                .isInstanceOfSatisfying(BusinessException.class, e ->
                        assertThat(e.getErrorCode()).isEqualTo(ErrorCode.STUDY_SESSION_NOT_FOUND));
    }
}
