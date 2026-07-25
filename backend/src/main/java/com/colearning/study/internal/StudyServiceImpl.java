package com.colearning.study.internal;

import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.study.StudyService;
import com.colearning.study.dto.request.CreateExamGoalRequest;
import com.colearning.study.dto.request.CreateSubjectRequest;
import com.colearning.study.dto.request.CreateTaskRequest;
import com.colearning.study.dto.request.UpdateExamGoalRequest;
import com.colearning.study.dto.request.UpdateSubjectRequest;
import com.colearning.study.dto.request.UpdateTaskRequest;
import com.colearning.study.dto.response.ExamGoalResponse;
import com.colearning.study.dto.response.SubjectResponse;
import com.colearning.study.dto.response.TaskResponse;
import com.colearning.study.internal.entity.ExamGoal;
import com.colearning.study.internal.entity.Subject;
import com.colearning.study.internal.entity.StudyTask;
import com.colearning.study.internal.repository.ExamGoalRepository;
import com.colearning.study.internal.repository.FocusSessionRepository;
import com.colearning.study.internal.repository.StudyTaskRepository;
import com.colearning.study.internal.repository.SubjectRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StudyServiceImpl implements StudyService {

    private final ExamGoalRepository examGoalRepository;
    private final SubjectRepository subjectRepository;
    private final StudyTaskRepository studyTaskRepository;
    private final FocusSessionRepository focusSessionRepository;

    // ===== Exam Goals =====

    @Override
    @Transactional
    public ExamGoalResponse createGoal(Long userId, CreateExamGoalRequest request) {
        ExamGoal goal = ExamGoal.builder()
                .userId(userId)
                .examName(request.examName())
                .examDate(request.examDate())
                .targetScore(request.targetScore())
                .status("ACTIVE")
                .build();
        goal = examGoalRepository.save(goal);
        return toGoalResponse(goal);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExamGoalResponse> listGoals(Long userId) {
        return examGoalRepository.findByUserIdOrderByExamDateAsc(userId).stream()
                .map(this::toGoalResponse)
                .toList();
    }

    @Override
    @Transactional
    public ExamGoalResponse updateGoal(Long userId, Long goalId, UpdateExamGoalRequest request) {
        ExamGoal goal = findGoalByIdAndUserId(goalId, userId);
        if (request.examName() != null) goal.setExamName(request.examName());
        if (request.examDate() != null) goal.setExamDate(request.examDate());
        if (request.targetScore() != null) goal.setTargetScore(request.targetScore());
        if (request.status() != null) goal.setStatus(request.status());
        return toGoalResponse(goal);
    }

    @Override
    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        ExamGoal goal = findGoalByIdAndUserId(goalId, userId);
        examGoalRepository.delete(goal);
    }

    // ===== Subjects =====

    @Override
    @Transactional
    public SubjectResponse createSubject(Long userId, CreateSubjectRequest request) {
        if (subjectRepository.existsByUserIdAndName(userId, request.name())) {
            throw BusinessException.of(ErrorCode.STUDY_SUBJECT_NOT_FOUND,
                    "同名科目已存在: " + request.name());
        }
        Subject subject = Subject.builder()
                .userId(userId)
                .name(request.name())
                .color(request.color() != null ? request.color() : "#2080F0")
                .sortOrder(0)
                .build();
        subject = subjectRepository.save(subject);
        return toSubjectResponse(subject);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubjectResponse> listSubjects(Long userId) {
        return subjectRepository.findByUserIdOrderBySortOrderAsc(userId).stream()
                .map(this::toSubjectResponse)
                .toList();
    }

    @Override
    @Transactional
    public SubjectResponse updateSubject(Long userId, Long subjectId, UpdateSubjectRequest request) {
        Subject subject = findSubjectByIdAndUserId(subjectId, userId);
        if (request.name() != null) subject.setName(request.name());
        if (request.color() != null) subject.setColor(request.color());
        if (request.sortOrder() != null) subject.setSortOrder(request.sortOrder());
        return toSubjectResponse(subject);
    }

    @Override
    @Transactional
    public void deleteSubject(Long userId, Long subjectId) {
        Subject subject = findSubjectByIdAndUserId(subjectId, userId);
        subjectRepository.delete(subject);
    }

    // ===== Tasks =====

    @Override
    @Transactional
    public TaskResponse createTask(Long userId, CreateTaskRequest request) {
        // Validate subject ownership if provided
        String subjectName = null;
        String subjectColor = null;
        if (request.subjectId() != null) {
            Subject subject = findSubjectByIdAndUserId(request.subjectId(), userId);
            subjectName = subject.getName();
            subjectColor = subject.getColor();
        }
        // Validate goal ownership if provided
        if (request.examGoalId() != null) {
            findGoalByIdAndUserId(request.examGoalId(), userId);
        }

        StudyTask task = StudyTask.builder()
                .userId(userId)
                .subjectId(request.subjectId())
                .examGoalId(request.examGoalId())
                .title(request.title())
                .description(request.description())
                .status("TODO")
                .dueDate(request.dueDate())
                .sortOrder(0)
                .build();
        task = studyTaskRepository.save(task);
        return toTaskResponse(task, subjectName, subjectColor);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listTasks(Long userId, String status, Long subjectId) {
        List<StudyTask> tasks;
        if (status != null && subjectId != null) {
            tasks = studyTaskRepository.findByUserIdAndStatusAndSubjectIdOrderBySortOrderAsc(
                    userId, status, subjectId);
        } else if (status != null) {
            tasks = studyTaskRepository.findByUserIdAndStatusOrderBySortOrderAsc(userId, status);
        } else if (subjectId != null) {
            tasks = studyTaskRepository.findByUserIdAndSubjectIdOrderBySortOrderAsc(userId, subjectId);
        } else {
            tasks = studyTaskRepository.findByUserIdOrderBySortOrderAsc(userId);
        }

        // Batch-load subject names for efficiency
        Map<Long, Subject> subjectMap = subjectRepository
                .findByUserIdOrderBySortOrderAsc(userId).stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        return tasks.stream()
                .map(t -> {
                    Subject s = t.getSubjectId() != null ? subjectMap.get(t.getSubjectId()) : null;
                    return toTaskResponse(t,
                            s != null ? s.getName() : null,
                            s != null ? s.getColor() : null);
                })
                .toList();
    }

    @Override
    @Transactional
    public TaskResponse updateTask(Long userId, Long taskId, UpdateTaskRequest request) {
        StudyTask task = findTaskByIdAndUserId(taskId, userId);

        String subjectName = null;
        String subjectColor = null;
        if (request.subjectId() != null) {
            Subject subject = findSubjectByIdAndUserId(request.subjectId(), userId);
            subjectName = subject.getName();
            subjectColor = subject.getColor();
            task.setSubjectId(request.subjectId());
        }
        if (request.title() != null) task.setTitle(request.title());
        if (request.description() != null) task.setDescription(request.description());
        if (request.status() != null) task.setStatus(request.status());
        if (request.dueDate() != null) task.setDueDate(request.dueDate());
        if (request.sortOrder() != null) task.setSortOrder(request.sortOrder());

        return toTaskResponse(task, subjectName, subjectColor);
    }

    @Override
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        StudyTask task = findTaskByIdAndUserId(taskId, userId);
        studyTaskRepository.delete(task);
    }

    // ===== Private helpers =====

    private ExamGoal findGoalByIdAndUserId(Long goalId, Long userId) {
        return examGoalRepository.findById(goalId)
                .filter(g -> g.getUserId().equals(userId))
                .orElseThrow(() -> BusinessException.of(ErrorCode.STUDY_GOAL_NOT_FOUND));
    }

    private Subject findSubjectByIdAndUserId(Long subjectId, Long userId) {
        return subjectRepository.findById(subjectId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> BusinessException.of(ErrorCode.STUDY_SUBJECT_NOT_FOUND));
    }

    private StudyTask findTaskByIdAndUserId(Long taskId, Long userId) {
        return studyTaskRepository.findById(taskId)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> BusinessException.of(ErrorCode.STUDY_TASK_NOT_FOUND));
    }

    private ExamGoalResponse toGoalResponse(ExamGoal goal) {
        long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(
                LocalDate.now(), goal.getExamDate());
        return new ExamGoalResponse(
                goal.getId(),
                goal.getExamName(),
                goal.getExamDate(),
                goal.getTargetScore(),
                goal.getStatus(),
                daysRemaining,
                goal.getCreatedAt(),
                goal.getUpdatedAt()
        );
    }

    private SubjectResponse toSubjectResponse(Subject subject) {
        return new SubjectResponse(
                subject.getId(),
                subject.getName(),
                subject.getColor(),
                subject.getSortOrder(),
                subject.getCreatedAt(),
                subject.getUpdatedAt()
        );
    }

    private TaskResponse toTaskResponse(StudyTask task, String subjectName, String subjectColor) {
        return new TaskResponse(
                task.getId(),
                task.getSubjectId(),
                subjectName,
                subjectColor,
                task.getExamGoalId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus(),
                task.getDueDate(),
                task.getSortOrder(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
