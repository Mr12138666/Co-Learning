package com.colearning.gamification.internal.repository;

import com.colearning.gamification.internal.entity.DailyTask;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyTaskRepository extends JpaRepository<DailyTask, Long> {

    List<DailyTask> findByUserIdAndTaskDate(Long userId, LocalDate taskDate);

    List<DailyTask> findByUserIdAndTaskDateAndStatus(Long userId, LocalDate taskDate, String status);

    Optional<DailyTask> findByUserIdAndTaskDateAndTaskType(Long userId, LocalDate taskDate, String taskType);

    void deleteByTaskDateBefore(LocalDate date);
}