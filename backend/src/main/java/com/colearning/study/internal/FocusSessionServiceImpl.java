package com.colearning.study.internal;

import com.colearning.common.event.FocusSessionFinishedEvent;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.gamification.DailyTaskService;
import com.colearning.gamification.GamificationService;
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
    private final GamificationService gamificationService;
    private final DailyTaskService dailyTaskService;

    @Value("${app.focus.max-session-hours:8}")
    private int maxSessionHours;

    @Value("${app.focus.max-pause-hours:1}")
    private int maxPauseHours;

    @Value("${app.focus.grace-period-minutes:30}")
    private int gracePeriodMinutes;

    private static final String GRACE_REASON_LEARNING = "LEARNING_LIMIT";
    private static final String GRACE_REASON_PAUSE = "PAUSE_LIMIT";
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
        // 8h学习上限宽限期内不允许恢复，只能结束
        if (GRACE_REASON_LEARNING.equals(session.getGraceReason())) {
            throw BusinessException.of(ErrorCode.STUDY_INVALID_STATE_TRANSITION,
                    "已达到学习时长上限，请结束当前会话");
        }
        Instant now = Instant.now(clock);
        // Accumulate paused seconds
        int additionalPaused = (int) Duration.between(session.getPausedAt(), now).getSeconds();
        session.setPausedSeconds(session.getPausedSeconds() + additionalPaused);
        session.setStatus("ACTIVE");
        session.setResumedAt(now);
        session.setPausedAt(null);
        // 清除宽限期（暂停超限后用户选择恢复）
        session.setGraceDeadline(null);
        session.setGraceReason(null);
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
        session.setGraceDeadline(null);
        session.setGraceReason(null);

        log.info("Focus session finished: userId={}, sessionId={}, effectiveSec={}",
                userId, sessionId, effective);

        // Directly award EXP and tokens for reliability
        // EXP: every 10 min = 1 EXP (600 seconds)
        // Tokens: every 10 min = 1 token (600 seconds) - increased from 30 min
        try {
            int exp = Math.max(1, effective / 600);
            int tokens = Math.max(1, effective / 600);
            gamificationService.addExp(userId, exp);
            gamificationService.addTokens(userId, tokens);
            gamificationService.checkAndUnlockAchievements(userId);
            log.info("Awarded EXP={}, tokens={} for focus session: userId={}", exp, tokens, userId);
        } catch (Exception e) {
            log.error("Failed to award EXP/tokens for userId={}: {}", userId, e.getMessage(), e);
        }
        
        // Update daily task progress
        try {
            dailyTaskService.onFocusSessionFinished(userId, effective);
        } catch (Exception e) {
            log.error("Failed to update daily tasks for userId={}: {}", userId, e.getMessage());
        }

        // Publish event for stats, leaderboard
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
        session.setGraceDeadline(null);
        session.setGraceReason(null);

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
                            session.getResumedAt(),
                            session.getPausedSeconds(),
                            elapsed,
                            session.getSubjectId(),
                            session.getTaskId(),
                            session.getGraceDeadline(),
                            session.getGraceReason()
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
        Instant now = Instant.now(clock);
        int maxEffectiveSeconds = maxSessionHours * 3600;
        int maxPauseSeconds = maxPauseHours * 3600;

        List<FocusSession> ongoing = focusSessionRepository.findAllOngoing();
        for (FocusSession session : ongoing) {
            try {
                // 1. 检查宽限期是否已过期（最高优先级）
                if (session.getGraceDeadline() != null && now.isAfter(session.getGraceDeadline())) {
                    log.warn("Grace period expired, aborting session: userId={}, sessionId={}, reason={}",
                            session.getUserId(), session.getId(), session.getGraceReason());
                    abort(session.getUserId(), session.getId());
                    continue;
                }

                // 2. 检查 ACTIVE 会话是否达到8h有效学习上限
                if (session.isActive() && session.getGraceDeadline() == null) {
                    int elapsed = session.computeElapsedSeconds(now);
                    if (elapsed >= maxEffectiveSeconds) {
                        session.setStatus("PAUSED");
                        session.setPausedAt(now);
                        session.setResumedAt(null);
                        session.setGraceDeadline(now.plus(Duration.ofMinutes(gracePeriodMinutes)));
                        session.setGraceReason(GRACE_REASON_LEARNING);
                        log.warn("Auto-paused session reached {}h learning limit: userId={}, sessionId={}, elapsedSec={}",
                                maxSessionHours, session.getUserId(), session.getId(), elapsed);
                    }
                }

                // 3. 检查 PAUSED 会话是否超过1h暂停上限
                if (session.isPaused() && session.getGraceDeadline() == null
                        && session.getPausedAt() != null) {
                    long pauseDuration = Duration.between(session.getPausedAt(), now).getSeconds();
                    if (pauseDuration >= maxPauseSeconds) {
                        session.setGraceDeadline(now.plus(Duration.ofMinutes(gracePeriodMinutes)));
                        session.setGraceReason(GRACE_REASON_PAUSE);
                        log.warn("Session paused too long, entering grace period: userId={}, sessionId={}, pauseSec={}",
                                session.getUserId(), session.getId(), pauseDuration);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to process session {}: {}", session.getId(), e.getMessage());
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
