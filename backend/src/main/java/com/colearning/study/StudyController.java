package com.colearning.study;

import com.colearning.common.dto.ApiResponse;
import com.colearning.common.security.SecurityUtils;
import com.colearning.study.dto.request.BulkPlannedDateRequest;
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
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for study management: exam goals, subjects, and tasks.
 */
@RestController
@RequestMapping("/api/study")
@RequiredArgsConstructor
public class StudyController {

    private final StudyService studyService;

    // ===== Exam Goals =====

    @PostMapping("/goals")
    public ResponseEntity<ApiResponse<ExamGoalResponse>> createGoal(
            @Valid @RequestBody CreateExamGoalRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.createGoal(userId, request)));
    }

    @GetMapping("/goals")
    public ResponseEntity<ApiResponse<List<ExamGoalResponse>>> listGoals() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.listGoals(userId)));
    }

    @PutMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<ExamGoalResponse>> updateGoal(
            @PathVariable Long goalId,
            @Valid @RequestBody UpdateExamGoalRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.updateGoal(userId, goalId, request)));
    }

    @DeleteMapping("/goals/{goalId}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(@PathVariable Long goalId) {
        Long userId = SecurityUtils.getCurrentUserId();
        studyService.deleteGoal(userId, goalId);
        return ResponseEntity.ok(ApiResponse.message("考试目标已删除"));
    }

    // ===== Subjects =====

    @PostMapping("/subjects")
    public ResponseEntity<ApiResponse<SubjectResponse>> createSubject(
            @Valid @RequestBody CreateSubjectRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.createSubject(userId, request)));
    }

    @GetMapping("/subjects")
    public ResponseEntity<ApiResponse<List<SubjectResponse>>> listSubjects() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.listSubjects(userId)));
    }

    @PutMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<SubjectResponse>> updateSubject(
            @PathVariable Long subjectId,
            @Valid @RequestBody UpdateSubjectRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.updateSubject(userId, subjectId, request)));
    }

    @DeleteMapping("/subjects/{subjectId}")
    public ResponseEntity<ApiResponse<Void>> deleteSubject(@PathVariable Long subjectId) {
        Long userId = SecurityUtils.getCurrentUserId();
        studyService.deleteSubject(userId, subjectId);
        return ResponseEntity.ok(ApiResponse.message("科目已删除"));
    }

    // ===== Tasks =====

    @PostMapping("/tasks")
    public ResponseEntity<ApiResponse<TaskResponse>> createTask(
            @Valid @RequestBody CreateTaskRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.createTask(userId, request)));
    }

    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> listTasks(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long subjectId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate plannedDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        if (plannedDate != null) {
            return ResponseEntity.ok(ApiResponse.ok(
                    studyService.listTasksByDateRange(userId, plannedDate, plannedDate)));
        }
        return ResponseEntity.ok(ApiResponse.ok(studyService.listTasks(userId, status, subjectId)));
    }

    @GetMapping("/tasks/inbox")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> listInboxTasks() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.listInboxTasks(userId)));
    }

    @GetMapping("/tasks/today")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> listTodayTasks() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.listTodayTasks(userId)));
    }

    @GetMapping("/tasks/overdue")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> listOverdueTasks() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.listOverdueTasks(userId)));
    }

    @GetMapping("/tasks/planner")
    public ResponseEntity<ApiResponse<List<TaskResponse>>> listTasksByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.listTasksByDateRange(userId, startDate, endDate)));
    }

    @GetMapping("/tasks/quadrant")
    public ResponseEntity<ApiResponse<Map<String, List<TaskResponse>>>> listTasksByQuadrant() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.listTasksByQuadrant(userId)));
    }

    @PostMapping("/tasks/bulk-planned-date")
    public ResponseEntity<ApiResponse<Void>> bulkUpdatePlannedDate(
            @Valid @RequestBody BulkPlannedDateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        studyService.bulkUpdatePlannedDate(userId, request.taskIds(), request.plannedDate());
        return ResponseEntity.ok(ApiResponse.message("批量更新计划日期成功"));
    }

    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<TaskResponse>> updateTask(
            @PathVariable Long taskId,
            @Valid @RequestBody UpdateTaskRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.updateTask(userId, taskId, request)));
    }

    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId();
        studyService.deleteTask(userId, taskId);
        return ResponseEntity.ok(ApiResponse.message("任务已删除"));
    }

    // ===== Tags =====

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> listTags() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.listTags(userId)));
    }

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @Valid @RequestBody CreateTagRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(studyService.createTag(userId, request)));
    }

    @DeleteMapping("/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long tagId) {
        Long userId = SecurityUtils.getCurrentUserId();
        studyService.deleteTag(userId, tagId);
        return ResponseEntity.ok(ApiResponse.message("标签已删除"));
    }
}
