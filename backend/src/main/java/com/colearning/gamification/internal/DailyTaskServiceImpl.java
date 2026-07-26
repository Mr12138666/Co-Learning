package com.colearning.gamification.internal;

import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.gamification.DailyTaskService;
import com.colearning.gamification.dto.response.DailyTaskResponse;
import com.colearning.gamification.internal.entity.DailyTask;
import com.colearning.gamification.internal.entity.UserExp;
import com.colearning.gamification.internal.repository.DailyTaskRepository;
import com.colearning.gamification.internal.repository.UserExpRepository;
import com.colearning.auth.internal.repository.UserRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of DailyTaskService.
 * 
 * Daily task types and rewards:
 * - FOCUS_ONCE: Complete 1 focus session → 5 tokens
 * - FOCUS_30MIN: Accumulate 30 min focus → 10 tokens
 * - FOCUS_60MIN: Accumulate 60 min focus → 15 tokens
 * - FEED_PET: Feed pet once → 3 tokens
 * - CHECKIN: Complete daily checkin → 5 tokens
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DailyTaskServiceImpl implements DailyTaskService {

    private final DailyTaskRepository dailyTaskRepository;
    private final UserExpRepository userExpRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    @Override
    @Transactional
    public List<DailyTaskResponse> getTodayTasks(Long userId) {
        LocalDate today = LocalDate.now(clock.getZone());
        List<DailyTask> existing = dailyTaskRepository.findByUserIdAndTaskDate(userId, today);
        
        if (existing.isEmpty()) {
            List<DailyTask> newTasks = createTodayTasks(userId, today);
            return newTasks.stream().map(DailyTaskResponse::from).toList();
        }
        
        return existing.stream().map(DailyTaskResponse::from).toList();
    }

    @Override
    @Transactional
    public DailyTaskResponse claimReward(Long userId, Long taskId) {
        DailyTask task = dailyTaskRepository.findById(taskId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ITEM_NOT_FOUND));
        
        if (!task.getUserId().equals(userId)) {
            throw BusinessException.of(ErrorCode.ITEM_NOT_FOUND);
        }
        
        if (!task.isCompleted()) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "任务未完成");
        }
        
        // Award tokens directly
        UserExp userExp = userExpRepository.findByUserId(userId)
                .orElse(UserExp.builder().userId(userId).tokens(0).build());
        userExp.setTokens(userExp.getTokens() + task.getRewardTokens());
        userExpRepository.save(userExp);
        
        log.info("User {} claimed daily task reward: taskId={}, tokens={}", 
                userId, taskId, task.getRewardTokens());
        
        // Mark as claimed
        task.setStatus("CLAIMED");
        return DailyTaskResponse.from(dailyTaskRepository.save(task));
    }

    @Override
    @Transactional
    public void onFocusSessionFinished(Long userId, int effectiveSeconds) {
        LocalDate today = LocalDate.now(clock.getZone());
        
        // Update FOCUS_ONCE task (count sessions)
        dailyTaskRepository.findByUserIdAndTaskDateAndTaskType(userId, today, "FOCUS_ONCE")
                .ifPresent(task -> {
                    if (!task.isClaimed()) {
                        task.setCurrentProgress(task.getCurrentProgress() + 1);
                        if (task.getCurrentProgress() >= task.getTargetValue()) {
                            task.setStatus("COMPLETED");
                        }
                        dailyTaskRepository.save(task);
                    }
                });
        
        // Update FOCUS_30MIN, FOCUS_60MIN and FOCUS_120MIN tasks (count seconds)
        dailyTaskRepository.findByUserIdAndTaskDateAndTaskType(userId, today, "FOCUS_30MIN")
                .ifPresent(task -> updateFocusTask(task, effectiveSeconds));
        
        dailyTaskRepository.findByUserIdAndTaskDateAndTaskType(userId, today, "FOCUS_60MIN")
                .ifPresent(task -> updateFocusTask(task, effectiveSeconds));
        
        dailyTaskRepository.findByUserIdAndTaskDateAndTaskType(userId, today, "FOCUS_120MIN")
                .ifPresent(task -> updateFocusTask(task, effectiveSeconds));
    }
    
    private void updateFocusTask(DailyTask task, int effectiveSeconds) {
        if (!task.isClaimed()) {
            task.setCurrentProgress(task.getCurrentProgress() + effectiveSeconds);
            if (task.getCurrentProgress() >= task.getTargetValue()) {
                task.setStatus("COMPLETED");
            }
            dailyTaskRepository.save(task);
        }
    }

    @Override
    @Transactional
    public void onFeedPet(Long userId) {
        LocalDate today = LocalDate.now(clock.getZone());
        dailyTaskRepository.findByUserIdAndTaskDateAndTaskType(userId, today, "FEED_PET")
                .ifPresent(task -> {
                    if (!task.isClaimed()) {
                        task.setCurrentProgress(task.getCurrentProgress() + 1);
                        if (task.getCurrentProgress() >= task.getTargetValue()) {
                            task.setStatus("COMPLETED");
                        }
                        dailyTaskRepository.save(task);
                    }
                });
    }

    @Override
    @Transactional
    public void onCheckinCompleted(Long userId) {
        LocalDate today = LocalDate.now(clock.getZone());
        dailyTaskRepository.findByUserIdAndTaskDateAndTaskType(userId, today, "CHECKIN")
                .ifPresent(task -> {
                    if (!task.isClaimed()) {
                        task.setCurrentProgress(task.getCurrentProgress() + 1);
                        if (task.getCurrentProgress() >= task.getTargetValue()) {
                            task.setStatus("COMPLETED");
                        }
                        dailyTaskRepository.save(task);
                    }
                });
    }

    @Override
    @Transactional
    public void onWriteJournal(Long userId) {
        LocalDate today = LocalDate.now(clock.getZone());
        dailyTaskRepository.findByUserIdAndTaskDateAndTaskType(userId, today, "WRITE_JOURNAL")
                .ifPresent(task -> {
                    if (!task.isClaimed()) {
                        task.setCurrentProgress(task.getCurrentProgress() + 1);
                        if (task.getCurrentProgress() >= task.getTargetValue()) {
                            task.setStatus("COMPLETED");
                        }
                        dailyTaskRepository.save(task);
                    }
                });
    }

    @Override
    @Transactional
    public void generateDailyTasks() {
        LocalDate today = LocalDate.now(clock.getZone());
        List<Long> userIds = userRepository.findAll().stream()
                .map(u -> u.getId())
                .toList();
        
        int createdCount = 0;
        for (Long userId : userIds) {
            List<DailyTask> existing = dailyTaskRepository.findByUserIdAndTaskDate(userId, today);
            if (existing.isEmpty()) {
                createTodayTasks(userId, today);
                createdCount++;
            }
        }
        log.info("Generated daily tasks for {} users on {}", createdCount, today);
    }

    @Override
    @Transactional
    public void cleanupOldTasks() {
        LocalDate cutoff = LocalDate.now(clock.getZone()).minusDays(7);
        dailyTaskRepository.deleteByTaskDateBefore(cutoff);
        log.info("Cleaned up tasks older than {}", cutoff);
    }

    // ===== Scheduled tasks =====
    
    @Scheduled(cron = "0 0 0 * * ?")  // Midnight every day
    public void scheduledGenerateDailyTasks() {
        generateDailyTasks();
    }
    
    @Scheduled(cron = "0 0 1 * * ?")  // 1 AM every day
    public void scheduledCleanupOldTasks() {
        cleanupOldTasks();
    }

    // ===== Private helpers =====

    private List<DailyTask> createTodayTasks(Long userId, LocalDate date) {
        List<DailyTask> tasks = new ArrayList<>();
        
        // 1. Complete 1 focus session → 5 tokens
        tasks.add(DailyTask.builder()
                .userId(userId)
                .taskType("FOCUS_ONCE")
                .title("开始专注")
                .description("完成一次专注会话")
                .targetValue(1)
                .currentProgress(0)
                .rewardTokens(5)
                .status("IN_PROGRESS")
                .taskDate(date)
                .build());
        
        // 2. Accumulate 30 min focus → 10 tokens
        tasks.add(DailyTask.builder()
                .userId(userId)
                .taskType("FOCUS_30MIN")
                .title("专注达人")
                .description("累计专注30分钟")
                .targetValue(30 * 60)
                .currentProgress(0)
                .rewardTokens(10)
                .status("IN_PROGRESS")
                .taskDate(date)
                .build());
        
        // 3. Accumulate 60 min focus → 15 tokens
        tasks.add(DailyTask.builder()
                .userId(userId)
                .taskType("FOCUS_60MIN")
                .title("学霸之路")
                .description("累计专注60分钟")
                .targetValue(60 * 60)
                .currentProgress(0)
                .rewardTokens(15)
                .status("IN_PROGRESS")
                .taskDate(date)
                .build());
        
        // 4. Accumulate 120 min focus → 20 tokens
        tasks.add(DailyTask.builder()
                .userId(userId)
                .taskType("FOCUS_120MIN")
                .title("学习狂魔")
                .description("累计专注120分钟")
                .targetValue(120 * 60)
                .currentProgress(0)
                .rewardTokens(20)
                .status("IN_PROGRESS")
                .taskDate(date)
                .build());
        
        // 5. Feed pet once → 3 tokens
        tasks.add(DailyTask.builder()
                .userId(userId)
                .taskType("FEED_PET")
                .title("投喂宠物")
                .description("给宠物喂食一次")
                .targetValue(1)
                .currentProgress(0)
                .rewardTokens(3)
                .status("IN_PROGRESS")
                .taskDate(date)
                .build());
        
        // 6. Complete daily checkin → 5 tokens
        tasks.add(DailyTask.builder()
                .userId(userId)
                .taskType("CHECKIN")
                .title("每日打卡")
                .description("完成今日打卡")
                .targetValue(1)
                .currentProgress(0)
                .rewardTokens(5)
                .status("IN_PROGRESS")
                .taskDate(date)
                .build());
        
        // 7. Write a journal → 5 tokens
        tasks.add(DailyTask.builder()
                .userId(userId)
                .taskType("WRITE_JOURNAL")
                .title("记录学习")
                .description("写一篇学习日志")
                .targetValue(1)
                .currentProgress(0)
                .rewardTokens(5)
                .status("IN_PROGRESS")
                .taskDate(date)
                .build());
        
        return dailyTaskRepository.saveAll(tasks);
    }
}