package com.colearning.study.internal;

import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.study.StudyService;
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
import com.colearning.study.internal.entity.ExamGoal;
import com.colearning.study.internal.entity.Subject;
import com.colearning.study.internal.entity.StudyTask;
import com.colearning.study.internal.entity.Tag;
import com.colearning.study.internal.repository.ExamGoalRepository;
import com.colearning.study.internal.repository.FocusSessionRepository;
import com.colearning.study.internal.repository.StudyTaskRepository;
import com.colearning.study.internal.repository.SubjectRepository;
import com.colearning.study.internal.repository.TagRepository;
import java.time.LocalDate;
import java.util.LinkedHashMap;
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
    private final TagRepository tagRepository;

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
            throw BusinessException.of(ErrorCode.CONFLICT,
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
                .plannedDate(request.plannedDate())
                .scheduledStart(request.scheduledStart())
                .scheduledEnd(request.scheduledEnd())
                .estimatedMinutes(request.estimatedMinutes())
                .urgent(request.urgent() != null ? request.urgent() : false)
                .important(request.important() != null ? request.important() : false)
                .build();
        task = studyTaskRepository.save(task);

        // Handle tag associations
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            saveTaskTags(task.getId(), request.tagIds());
        }

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

        return toTaskResponseList(userId, tasks);
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
        if (request.plannedDate() != null) task.setPlannedDate(request.plannedDate());
        if (request.scheduledStart() != null) task.setScheduledStart(request.scheduledStart());
        if (request.scheduledEnd() != null) task.setScheduledEnd(request.scheduledEnd());
        if (request.estimatedMinutes() != null) task.setEstimatedMinutes(request.estimatedMinutes());
        if (request.urgent() != null) task.setUrgent(request.urgent());
        if (request.important() != null) task.setImportant(request.important());

        // Handle tag associations
        if (request.tagIds() != null) {
            tagRepository.deleteTaskTagsByTaskId(task.getId());
            if (!request.tagIds().isEmpty()) {
                saveTaskTags(task.getId(), request.tagIds());
            }
        }

        return toTaskResponse(task, subjectName, subjectColor);
    }

    @Override
    @Transactional
    public void deleteTask(Long userId, Long taskId) {
        StudyTask task = findTaskByIdAndUserId(taskId, userId);
        studyTaskRepository.delete(task);
    }

    // ===== Task Scheduling =====

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listInboxTasks(Long userId) {
        List<StudyTask> tasks = studyTaskRepository.findInboxTasks(userId);
        return toTaskResponseList(userId, tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listTodayTasks(Long userId) {
        List<StudyTask> tasks = studyTaskRepository.findPlannedTasksByDate(userId, LocalDate.now());
        return toTaskResponseList(userId, tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listOverdueTasks(Long userId) {
        List<StudyTask> tasks = studyTaskRepository.findOverdueTasks(userId, LocalDate.now());
        return toTaskResponseList(userId, tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TaskResponse> listTasksByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<StudyTask> tasks = studyTaskRepository.findTasksByDateRange(userId, startDate, endDate);
        return toTaskResponseList(userId, tasks);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, List<TaskResponse>> listTasksByQuadrant(Long userId) {
        Map<String, List<TaskResponse>> result = new LinkedHashMap<>();
        result.put("urgent-important", toTaskResponseList(userId,
                studyTaskRepository.findByQuadrant(userId, true, true)));
        result.put("not-urgent-important", toTaskResponseList(userId,
                studyTaskRepository.findByQuadrant(userId, false, true)));
        result.put("urgent-not-important", toTaskResponseList(userId,
                studyTaskRepository.findByQuadrant(userId, true, false)));
        result.put("not-urgent-not-important", toTaskResponseList(userId,
                studyTaskRepository.findByQuadrant(userId, false, false)));
        return result;
    }

    @Override
    @Transactional
    public void bulkUpdatePlannedDate(Long userId, List<Long> taskIds, LocalDate plannedDate) {
        Map<Long, StudyTask> taskMap = studyTaskRepository.findAllById(taskIds).stream()
                .collect(Collectors.toMap(StudyTask::getId, Function.identity()));
        for (Long taskId : taskIds) {
            StudyTask task = taskMap.get(taskId);
            if (task == null || !task.getUserId().equals(userId)) {
                throw BusinessException.of(ErrorCode.STUDY_TASK_NOT_FOUND);
            }
            task.setPlannedDate(plannedDate);
        }
    }

    // ===== Tags =====

    @Override
    @Transactional
    public TagResponse createTag(Long userId, CreateTagRequest request) {
        if (tagRepository.existsByUserIdAndName(userId, request.name())) {
            throw BusinessException.of(ErrorCode.CONFLICT,
                    "同名标签已存在: " + request.name());
        }
        Tag tag = Tag.builder()
                .userId(userId)
                .name(request.name())
                .color(request.color() != null ? request.color() : "#6b7280")
                .build();
        tag = tagRepository.save(tag);
        return toTagResponse(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TagResponse> listTags(Long userId) {
        return tagRepository.findByUserIdOrderByNameAsc(userId).stream()
                .map(this::toTagResponse)
                .toList();
    }

    @Override
    @Transactional
    public void deleteTag(Long userId, Long tagId) {
        Tag tag = tagRepository.findById(tagId)
                .filter(t -> t.getUserId().equals(userId))
                .orElseThrow(() -> BusinessException.of(ErrorCode.NOT_FOUND, "标签不存在"));
        tagRepository.delete(tag);
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

    private void saveTaskTags(Long taskId, List<Long> tagIds) {
        for (Long tagId : tagIds) {
            tagRepository.insertTaskTag(taskId, tagId);
        }
    }

    private List<TagResponse> loadTaskTags(Long taskId) {
        return tagRepository.findByTaskId(taskId).stream()
                .map(this::toTagResponse)
                .toList();
    }

    private List<TaskResponse> toTaskResponseList(Long userId, List<StudyTask> tasks) {
        if (tasks.isEmpty()) {
            return List.of();
        }

        Map<Long, Subject> subjectMap = subjectRepository
                .findByUserIdOrderBySortOrderAsc(userId).stream()
                .collect(Collectors.toMap(Subject::getId, Function.identity()));

        List<Long> taskIds = tasks.stream().map(StudyTask::getId).toList();

        // Batch load tags for all tasks (avoids per-task queries)
        Map<Long, Tag> tagMap = tagRepository.findByUserIdOrderByNameAsc(userId).stream()
                .collect(Collectors.toMap(Tag::getId, Function.identity()));
        Map<Long, List<TagResponse>> tagsByTaskId = new LinkedHashMap<>();
        for (Object[] pair : tagRepository.findTaskTagPairsByTaskIds(taskIds)) {
            Long taskId = ((Number) pair[0]).longValue();
            Long tagId = ((Number) pair[1]).longValue();
            Tag tag = tagMap.get(tagId);
            if (tag != null) {
                tagsByTaskId.computeIfAbsent(taskId, k -> new java.util.ArrayList<>())
                        .add(toTagResponse(tag));
            }
        }
        tagsByTaskId.values().forEach(list ->
                list.sort(java.util.Comparator.comparing(TagResponse::name)));

        // Batch sum focus seconds for all tasks (avoids per-task queries)
        Map<Long, Long> focusSecondsByTaskId = new LinkedHashMap<>();
        for (Object[] row : focusSessionRepository.sumEffectiveSecondsByTaskIds(taskIds)) {
            focusSecondsByTaskId.put((Long) row[0], ((Number) row[1]).longValue());
        }

        return tasks.stream()
                .map(t -> {
                    Subject s = t.getSubjectId() != null ? subjectMap.get(t.getSubjectId()) : null;
                    return buildTaskResponse(t,
                            s != null ? s.getName() : null,
                            s != null ? s.getColor() : null,
                            tagsByTaskId.getOrDefault(t.getId(), List.of()),
                            focusSecondsByTaskId.getOrDefault(t.getId(), 0L));
                })
                .toList();
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
        List<TagResponse> tags = loadTaskTags(task.getId());
        long totalFocusSeconds = focusSessionRepository.sumEffectiveSecondsByTaskId(task.getId());
        return buildTaskResponse(task, subjectName, subjectColor, tags, totalFocusSeconds);
    }

    private TaskResponse buildTaskResponse(StudyTask task, String subjectName, String subjectColor,
                                           List<TagResponse> tags, long totalFocusSeconds) {
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
                task.getPlannedDate(),
                task.getScheduledStart(),
                task.getScheduledEnd(),
                task.getEstimatedMinutes(),
                task.getUrgent(),
                task.getImportant(),
                tags,
                totalFocusSeconds,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }

    private TagResponse toTagResponse(Tag tag) {
        return new TagResponse(
                tag.getId(),
                tag.getName(),
                tag.getColor(),
                tag.getCreatedAt()
        );
    }
}
