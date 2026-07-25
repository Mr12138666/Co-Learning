package com.colearning.study.internal;

import com.colearning.common.event.FocusSessionFinishedEvent;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.study.FocusSessionService;
import com.colearning.study.dto.request.StartFocusRequest;
import com.colearning.study.dto.response.ActiveFocusResponse;
import com.colearning.study.dto.response.FocusSessionResponse;
import com.colearning.study.internal.entity.FocusSession;
import com.colearning.study.internal.repository.FocusSessionRepository;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FocusSessionServiceImpl implements FocusSessionService {

    private final FocusSessionRepository focusSessionRepository;
    private final Clock clock;
    private final ApplicationEventPublisher eventPublisher;

    @Value("${app.focus.max-session-hours:4}")
    private int maxSessionHours;

    private static final List<String> ONGOING_STATUSES = List.of("ACTIVE", "PAUSED");

    // ===== Start =====

    @Override
    @Transactional
    public FocusSessionResponse start(Long userId, StartFocusRequest request) {
        // Idempotency: if clientRequestId is provided, check if session already exists
        if (request.clientRequestId() != null && !request.clientRequestId().isBlank()) {
            Optional<FocusSession> existing = focusSessionRepository
                    .findByUserIdAndClientRequestId(userId, request.clientRequestId());
            if (existing.isPresent()) {
                return toResponse(existing.get());
            }
        }

        // Check no ongoing session
        Optional<FocusSession> active = focusSessionRepository
                .findByUserIdAndStatusIn(userId, ONGOING_STATUSES);
        if (active.isPresent()) {
            throw BusinessException.of(ErrorCode.STUDY_SESSION_ALREADY_ACTIVE,
                    "session id: " + active.get().getId());
        }

        Instant now = Instant.now(clock);
        FocusSession session = FocusSession.builder()
                .userId(userId)
                .subjectId(request.subjectId())
                .taskId(request.taskId())
                .status("ACTIVE")
                .startedAt(now)
                .pausedSeconds(0)
                .effectiveSeconds(0)
                .clientRequestId(request.clientRequestId())
                .build();
        session = focusSessionRepository.save(session);
        log.info("Focus session started: userId={}, sessionId={}", userId, session.getId());
        return toResponse(session);
    }

    // ===== Pause =====

    @Override
    @Transactional
    public FocusSessionResponse pause(Long userId, Long sessionId) {
        FocusSession session = findOwnedSession(userId, sessionId);
        if (!session.isActive()) {
            throw BusinessException.of(ErrorCode.STUDY_INVALID_STATE_TRANSITION,
                    "当前状态: " + session.getStatus() + ", 仅 ACTIVE 可暂停");
        }
        session.setStatus("PAUSED");
        session.setPausedAt(Instant.now(clock));
        session.setResumedAt(null);
        return toResponse(session);
    }

    // ===== Resume =====

    @Override
    @Transactional
    public FocusSessionResponse resume(Long userId, Long sessionId) {
        FocusSession session = findOwnedSession(userId, sessionId);
        if (!session.isPaused()) {
            throw BusinessException.of(ErrorCode.STUDY_INVALID_STATE_TRANSITION,
                    "当前状态: " + session.getStatus() + ", 仅 PAUSED 可恢复");
        }
        Instant now = Instant.now(clock);
        // Accumulate paused seconds
        int additionalPaused = (int) Duration.between(session.getPausedAt(), now).getSeconds();
        session.setPausedSeconds(session.getPausedSeconds() + additionalPaused);
        session.setStatus("ACTIVE");
        session.setResumedAt(now);
        session.setPausedAt(null);
        return toResponse(session);
    }

    // ===== Finish =====

    @Override
    @Transactional
    public FocusSessionResponse finish(Long userId, Long sessionId) {
        FocusSession session = findOwnedSession(userId, sessionId);
        if (session.isFinished() || "ABORTED".equals(session.getStatus())) {
            throw BusinessException.of(ErrorCode.STUDY_SESSION_ALREADY_FINISHED);
        }

        Instant now = Instant.now(clock);
        session.setEndedAt(now);

        // If paused, accumulate the final pause duration
        if (session.isPaused() && session.getPausedAt() != null) {
            int finalPause = (int) Duration.between(session.getPausedAt(), now).getSeconds();
            session.setPausedSeconds(session.getPausedSeconds() + finalPause);
            session.setPausedAt(null);
        }

        // Calculate effective seconds
        int effective = (int) Duration.between(session.getStartedAt(), now).getSeconds()
                - session.getPausedSeconds();
        effective = Math.max(0, effective);
        session.setEffectiveSeconds(effective);
        session.setStatus("FINISHED");

        log.info("Focus session finished: userId={}, sessionId={}, effectiveSec={}",
                userId, sessionId, effective);

        // Publish event for stats, leaderboard, experience
        LocalDate sessionDate = session.getStartedAt()
                .atZone(clock.getZone())
                .toLocalDate();

        FocusSessionFinishedEvent event = new FocusSessionFinishedEvent(
                session.getId(),
                userId,
                session.getSubjectId(),
                session.getTaskId(),
                effective,
                session.getStartedAt(),
                session.getEndedAt(),
                sessionDate
        );
        eventPublisher.publishEvent(event);

        return toResponse(session);
    }

    // ===== Abort =====

    @Override
    @Transactional
    public FocusSessionResponse abort(Long userId, Long sessionId) {
        FocusSession session = findOwnedSession(userId, sessionId);
        if (session.isFinished() || "ABORTED".equals(session.getStatus())) {
            throw BusinessException.of(ErrorCode.STUDY_SESSION_ALREADY_FINISHED);
        }

        Instant now = Instant.now(clock);
        session.setEndedAt(now);
        session.setStatus("ABORTED");

        // Aborted sessions don't count effective time
        session.setEffectiveSeconds(0);

        log.info("Focus session aborted: userId={}, sessionId={}", userId, sessionId);
        return toResponse(session);
    }

    // ===== Get active session =====

    @Override
    @Transactional(readOnly = true)
    public Optional<ActiveFocusResponse> getActiveSession(Long userId) {
        return focusSessionRepository
                .findByUserIdAndStatusIn(userId, ONGOING_STATUSES)
                .map(session -> {
                    int elapsed = session.computeElapsedSeconds(Instant.now(clock));
                    return new ActiveFocusResponse(
                            session.getId(),
                            session.getStatus(),
                            session.getStartedAt(),
                            session.getPausedAt(),
                            session.getPausedSeconds(),
                            elapsed,
                            session.getSubjectId(),
                            session.getTaskId()
                    );
                });
    }

    // ===== Get specific session =====

    @Override
    @Transactional(readOnly = true)
    public FocusSessionResponse getSession(Long userId, Long sessionId) {
        FocusSession session = findOwnedSession(userId, sessionId);
        return toResponse(session);
    }

    // ===== Scheduled timeout guardian =====

    @Scheduled(fixedRate = 60_000)  // Every 1 minute
    @Transactional
    public void timeoutGuardian() {
        Instant threshold = Instant.now(clock)
                .minus(Duration.ofHours(maxSessionHours));
        List<FocusSession> stale = focusSessionRepository.findActiveSessionsBefore(threshold);
        for (FocusSession session : stale) {
            try {
                log.warn("Auto-finishing stale session: userId={}, sessionId={}, startedAt={}",
                        session.getUserId(), session.getId(), session.getStartedAt());
                finish(session.getUserId(), session.getId());
            } catch (Exception e) {
                log.error("Failed to auto-finish session {}: {}", session.getId(), e.getMessage());
            }
        }
    }

    // ===== Private helpers =====

    private FocusSession findOwnedSession(Long userId, Long sessionId) {
        FocusSession session = focusSessionRepository.findById(sessionId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.STUDY_SESSION_NOT_FOUND));
        if (!session.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.STUDY_SESSION_NOT_FOUND);
        }
        return session;
    }

    private FocusSessionResponse toResponse(FocusSession session) {
        int elapsed = session.computeElapsedSeconds(Instant.now(clock));
        return new FocusSessionResponse(
                session.getId(),
                session.getSubjectId(),
                session.getTaskId(),
                session.getStatus(),
                session.getStartedAt(),
                session.getPausedAt(),
                session.getResumedAt(),
                session.getEndedAt(),
                session.getPausedSeconds(),
                session.getEffectiveSeconds(),
                elapsed,
                session.getCreatedAt(),
                session.getUpdatedAt()
        );
    }
}
