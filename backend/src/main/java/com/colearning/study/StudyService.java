package com.colearning.study;

import com.colearning.study.dto.request.CreateExamGoalRequest;
import com.colearning.study.dto.request.CreateSubjectRequest;
import com.colearning.study.dto.request.CreateTagRequest;
import com.colearning.study.dto.request.CreateTaskRequest;
import com.colearning.study.dto.request.UpdateExamGoalRequest;
import com.colearning.study.dto.request.UpdateSubjectRequest;
import com.colearning.study.dto.request.UpdateTaskRequest;
import com.colearning.study.dto.response.ExamGoalResponse;
import com.colearning.study.dto.response.SubjectResponse;
import com.colearning.study.dto.response.TagResponse;
import com.colearning.study.dto.response.TaskResponse;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    // ===== Task Scheduling =====

    List<TaskResponse> listInboxTasks(Long userId);

    List<TaskResponse> listTodayTasks(Long userId);

    List<TaskResponse> listOverdueTasks(Long userId);

    List<TaskResponse> listTasksByDateRange(Long userId, LocalDate startDate, LocalDate endDate);

    Map<String, List<TaskResponse>> listTasksByQuadrant(Long userId);

    void bulkUpdatePlannedDate(Long userId, List<Long> taskIds, LocalDate plannedDate);

    // ===== Tags =====

    TagResponse createTag(Long userId, CreateTagRequest request);

    List<TagResponse> listTags(Long userId);

    void deleteTag(Long userId, Long tagId);
}
