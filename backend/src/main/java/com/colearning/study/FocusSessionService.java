package com.colearning.study;

import com.colearning.study.dto.request.StartFocusRequest;
import com.colearning.study.dto.response.ActiveFocusResponse;
import com.colearning.study.dto.response.FocusSessionResponse;
import java.util.Optional;

/**
 * Core service for managing focus study sessions.
 *
 * <p>Server-authoritative timing: all session state transitions are validated
 * and persisted on the server. The client sends a clientRequestId for idempotency
 * on session start.
 *
 * <p>State machine: ACTIVE -> PAUSED -> ACTIVE (resume), ACTIVE/PAUSED -> FINISHED/ABORTED
 */
public interface FocusSessionService {

    /**
     * Start a new focus session. Idempotent via clientRequestId.
     *
     * @throws com.colearning.common.exception.BusinessException if an active session already exists
     */
    FocusSessionResponse start(Long userId, StartFocusRequest request);

    /**
     * Pause an active session.
     */
    FocusSessionResponse pause(Long userId, Long sessionId);

    /**
     * Resume a paused session.
     */
    FocusSessionResponse resume(Long userId, Long sessionId);

    /**
     * Finish a session (ACTIVE or PAUSED). Computes effective seconds and publishes event.
     */
    FocusSessionResponse finish(Long userId, Long sessionId);

    /**
     * Abort a session (user manually cancels without counting time).
     */
    FocusSessionResponse abort(Long userId, Long sessionId);

    /**
     * Get the active or paused session for a user, if any.
     */
    Optional<ActiveFocusResponse> getActiveSession(Long userId);

    /**
     * Get a specific session by ID (must belong to the user).
     */
    FocusSessionResponse getSession(Long userId, Long sessionId);
}
