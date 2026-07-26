package com.colearning.study.internal.repository;

import com.colearning.study.internal.entity.StudyTask;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {

    List<StudyTask> findByUserIdOrderBySortOrderAsc(Long userId);

    List<StudyTask> findByUserIdAndStatusOrderBySortOrderAsc(Long userId, String status);

    List<StudyTask> findByUserIdAndSubjectIdOrderBySortOrderAsc(Long userId, Long subjectId);

    List<StudyTask> findByUserIdAndStatusAndSubjectIdOrderBySortOrderAsc(
            Long userId, String status, Long subjectId);

    // Inbox: tasks without planned date (not DONE/ARCHIVED)
    @Query("SELECT t FROM StudyTask t WHERE t.userId = :userId AND t.plannedDate IS NULL AND t.status NOT IN ('DONE', 'ARCHIVED') ORDER BY t.sortOrder ASC")
    List<StudyTask> findInboxTasks(@Param("userId") Long userId);

    // Today: tasks planned for today
    @Query("SELECT t FROM StudyTask t WHERE t.userId = :userId AND t.plannedDate = :date AND t.status NOT IN ('DONE', 'ARCHIVED') ORDER BY t.sortOrder ASC")
    List<StudyTask> findPlannedTasksByDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    // Overdue: tasks with planned date before today that are not done
    @Query("SELECT t FROM StudyTask t WHERE t.userId = :userId AND t.plannedDate < :today AND t.status NOT IN ('DONE', 'ARCHIVED') ORDER BY t.plannedDate ASC, t.sortOrder ASC")
    List<StudyTask> findOverdueTasks(@Param("userId") Long userId, @Param("today") LocalDate today);

    // Planner: tasks in date range
    @Query("SELECT t FROM StudyTask t WHERE t.userId = :userId AND t.plannedDate BETWEEN :startDate AND :endDate ORDER BY t.plannedDate ASC, t.sortOrder ASC")
    List<StudyTask> findTasksByDateRange(@Param("userId") Long userId, @Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    // Board: tasks by urgent/important quadrant
    @Query("SELECT t FROM StudyTask t WHERE t.userId = :userId AND t.urgent = :urgent AND t.important = :important AND t.status NOT IN ('DONE', 'ARCHIVED') ORDER BY t.sortOrder ASC")
    List<StudyTask> findByQuadrant(@Param("userId") Long userId, @Param("urgent") boolean urgent, @Param("important") boolean important);

    // Filter by planned date
    List<StudyTask> findByUserIdAndPlannedDateOrderBySortOrderAsc(Long userId, LocalDate plannedDate);

    // Kanban: tasks by status
    List<StudyTask> findByUserIdAndStatusInOrderBySortOrderAsc(Long userId, List<String> statuses);

    // Count tasks by subject
    @Query("SELECT COUNT(t) FROM StudyTask t WHERE t.subjectId = :subjectId AND t.status NOT IN ('DONE', 'ARCHIVED')")
    long countActiveBySubjectId(@Param("subjectId") Long subjectId);
}
