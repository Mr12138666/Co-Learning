package com.colearning.study.internal.repository;

import com.colearning.study.internal.entity.ExamGoal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExamGoalRepository extends JpaRepository<ExamGoal, Long> {

    List<ExamGoal> findByUserIdOrderByExamDateAsc(Long userId);

    List<ExamGoal> findByUserIdAndStatusOrderByExamDateAsc(Long userId, String status);

    boolean existsByUserIdAndExamName(Long userId, String examName);
}
