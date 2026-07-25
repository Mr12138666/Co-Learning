package com.colearning.study.internal.repository;

import com.colearning.study.internal.entity.StudyTask;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTaskRepository extends JpaRepository<StudyTask, Long> {

    List<StudyTask> findByUserIdOrderBySortOrderAsc(Long userId);

    List<StudyTask> findByUserIdAndStatusOrderBySortOrderAsc(Long userId, String status);

    List<StudyTask> findByUserIdAndSubjectIdOrderBySortOrderAsc(Long userId, Long subjectId);

    List<StudyTask> findByUserIdAndStatusAndSubjectIdOrderBySortOrderAsc(
            Long userId, String status, Long subjectId);
}
