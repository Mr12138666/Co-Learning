package com.colearning.gamification.internal.repository;

import com.colearning.gamification.internal.entity.DailyTask;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {

    List<DailyTask> findByUserIdAndTaskDate(Long userId, LocalDate taskDate);

    List<DailyTask> findByUserIdAndTaskDateAndStatus(Long userId, LocalDate taskDate, String status);

    Optional<DailyTask> findByUserIdAndTaskDateAndTaskType(Long userId, LocalDate taskDate, String taskType);

    void deleteByTaskDateBefore(LocalDate date);

    /**
     * Atomically claim a completed task. Returns 1 if successfully claimed, 0 if already claimed.
     * This prevents double-claiming under concurrent requests.
     */
    @Modifying
    @Query("UPDATE DailyTask t SET t.status = 'CLAIMED' WHERE t.id = :taskId AND t.status = 'COMPLETED'")
    int claimTask(@Param("taskId") Long taskId);
}