package com.colearning.study;

import com.colearning.study.dto.request.CreateExamGoalRequest;
import com.colearning.study.dto.request.CreateSubjectRequest;
import com.colearning.study.dto.request.CreateTaskRequest;
import com.colearning.study.dto.request.UpdateExamGoalRequest;
import com.colearning.study.dto.request.UpdateSubjectRequest;
import com.colearning.study.dto.request.UpdateTaskRequest;
import com.colearning.study.dto.response.ExamGoalResponse;
import com.colearning.study.dto.response.SubjectResponse;
import com.colearning.study.dto.response.TaskResponse;
import java.util.List;

/**
 * Service for managing exam goals, subjects, and study tasks.
 */
public interface StudyService {

    // ===== Exam Goals =====

    ExamGoalResponse createGoal(Long userId, CreateExamGoalRequest request);

    List<ExamGoalResponse> listGoals(Long userId);

    ExamGoalResponse updateGoal(Long userId, Long goalId, UpdateExamGoalRequest request);

    void deleteGoal(Long userId, Long goalId);

    // ===== Subjects =====

    SubjectResponse createSubject(Long userId, CreateSubjectRequest request);

    List<SubjectResponse> listSubjects(Long userId);

    SubjectResponse updateSubject(Long userId, Long subjectId, UpdateSubjectRequest request);

    void deleteSubject(Long userId, Long subjectId);

    // ===== Tasks =====

    TaskResponse createTask(Long userId, CreateTaskRequest request);

    List<TaskResponse> listTasks(Long userId, String status, Long subjectId);

    TaskResponse updateTask(Long userId, Long taskId, UpdateTaskRequest request);

    void deleteTask(Long userId, Long taskId);
}
