package com.colearning.study.internal.repository;

import com.colearning.study.internal.entity.FocusSession;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FocusSessionRepository extends JpaRepository<FocusSession, Long> {

    Optional<FocusSession> findByUserIdAndStatusIn(Long userId, List<String> statuses);

    Optional<FocusSession> findByUserIdAndClientRequestId(Long userId, String clientRequestId);

    /**
     * Find all finished sessions for a user within a date range (based on started_at).
     */
    List<FocusSession> findByUserIdAndStatusAndStartedAtBetweenOrderByStartedAtAsc(
            Long userId, String status, Instant start, Instant end);

    /**
     * Find finished sessions for a specific date (based on started_at).
     */
    @Query("SELECT fs FROM FocusSession fs WHERE fs.userId = :userId " +
           "AND fs.status = 'FINISHED' " +
           "AND fs.startedAt >= :start AND fs.startedAt < :end " +
           "ORDER BY fs.startedAt ASC")
    List<FocusSession> findFinishedSessionsInRange(
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("end") Instant end);

    /**
     * Find sessions that have been active longer than the timeout threshold.
     * Used by the scheduled timeout guardian.
     */
    @Query("SELECT fs FROM FocusSession fs WHERE fs.status = 'ACTIVE' " +
           "AND fs.startedAt < :threshold")
    List<FocusSession> findActiveSessionsBefore(@Param("threshold") Instant threshold);

    /**
     * Aggregate total effective seconds for a user within a date range.
     */
    @Query("SELECT COALESCE(SUM(fs.effectiveSeconds), 0) FROM FocusSession fs " +
           "WHERE fs.userId = :userId AND fs.status = 'FINISHED' " +
           "AND fs.startedAt >= :start AND fs.startedAt < :end")
    int sumEffectiveSecondsInRange(
            @Param("userId") Long userId,
            @Param("start") Instant start,
            @Param("end") Instant end);
}
