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

    @Query("SELECT fs FROM FocusSession fs WHERE fs.userId IN :userIds AND fs.status IN ('ACTIVE', 'PAUSED')")
    List<FocusSession> findOngoingByUserIds(@Param("userIds") List<Long> userIds);

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
     * Find all ongoing sessions (ACTIVE or PAUSED).
     * Used by the scheduled timeout guardian.
     */
    @Query("SELECT fs FROM FocusSession fs WHERE fs.status IN ('ACTIVE', 'PAUSED')")
    List<FocusSession> findAllOngoing();

    /**
     * Sum effective seconds for a specific task (all finished sessions).
     */
    @Query("SELECT COALESCE(SUM(fs.effectiveSeconds), 0) FROM FocusSession fs WHERE fs.taskId = :taskId AND fs.status = 'FINISHED'")
    long sumEffectiveSecondsByTaskId(@Param("taskId") Long taskId);

    /**
     * Batch sum effective seconds per task to avoid N+1 when assembling task lists.
     */
    @Query("SELECT fs.taskId, SUM(fs.effectiveSeconds) FROM FocusSession fs " +
           "WHERE fs.taskId IN :taskIds AND fs.status = 'FINISHED' GROUP BY fs.taskId")
    List<Object[]> sumEffectiveSecondsByTaskIds(@Param("taskIds") List<Long> taskIds);

    /**
     * Sum effective seconds by user for all finished sessions.
     * Used to rebuild leaderboard from database.
     */
    @Query("SELECT fs.userId, SUM(fs.effectiveSeconds) FROM FocusSession fs " +
           "WHERE fs.status = 'FINISHED' GROUP BY fs.userId")
    List<Object[]> sumEffectiveSecondsByUserId();

    /**
     * Sum effective seconds by user for finished sessions within a date range.
     * Used to rebuild daily/weekly leaderboards from database.
     */
    @Query("SELECT fs.userId, SUM(fs.effectiveSeconds) FROM FocusSession fs " +
           "WHERE fs.status = 'FINISHED' " +
           "AND fs.startedAt >= :start AND fs.startedAt < :end " +
           "GROUP BY fs.userId")
    List<Object[]> sumEffectiveSecondsByUserIdInRange(
            @Param("start") Instant start,
            @Param("end") Instant end);

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
