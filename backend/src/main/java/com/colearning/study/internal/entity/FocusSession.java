package com.colearning.study.internal.entity;

import com.colearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Duration;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "focus_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FocusSession extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "subject_id")
    private Long subjectId;

    @Column(name = "task_id")
    private Long taskId;

    @Column(nullable = false)
    private String status;  // ACTIVE | PAUSED | FINISHED | ABORTED

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "paused_at")
    private Instant pausedAt;

    @Column(name = "resumed_at")
    private Instant resumedAt;

    @Column(name = "ended_at")
    private Instant endedAt;

    @Column(name = "paused_seconds", nullable = false)
    @Builder.Default
    private Integer pausedSeconds = 0;

    @Column(name = "effective_seconds", nullable = false)
    @Builder.Default
    private Integer effectiveSeconds = 0;

    @Column(name = "client_request_id", length = 64)
    private String clientRequestId;

    // --- Status helpers ---

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isPaused() {
        return "PAUSED".equals(status);
    }

    public boolean isFinished() {
        return "FINISHED".equals(status);
    }

    public boolean isOngoing() {
        return isActive() || isPaused();
    }

    /**
     * Compute the current elapsed seconds based on server time.
     * For ACTIVE sessions: now - started_at - paused_seconds
     * For PAUSED sessions: paused_at - started_at - paused_seconds
     * For FINISHED sessions: effective_seconds
     */
    public int computeElapsedSeconds(Instant now) {
        if (isFinished() || "ABORTED".equals(status)) {
            return effectiveSeconds;
        }
        Instant endPoint = isPaused() ? pausedAt : now;
        long totalSeconds = Duration.between(startedAt, endPoint).getSeconds();
        return (int) Math.max(0, totalSeconds - pausedSeconds);
    }
}
