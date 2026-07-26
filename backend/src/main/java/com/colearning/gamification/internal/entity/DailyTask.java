package com.colearning.gamification.internal.entity;

import com.colearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Daily task for gamification rewards.
 * Each user gets 5 tasks daily, resets at midnight.
 */
@Entity
@Table(name = "daily_tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyTask extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;  // FOCUS_ONCE, FOCUS_30MIN, FOCUS_60MIN, FEED_PET, CHECKIN, WRITE_JOURNAL

    @Column(nullable = false, length = 100)
    private String title;

    @Column(length = 500)
    private String description;

    @Column(name = "target_value", nullable = false)
    private Integer targetValue;

    @Column(name = "current_progress", nullable = false)
    @Builder.Default
    private Integer currentProgress = 0;

    @Column(name = "reward_tokens", nullable = false)
    private Integer rewardTokens;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String status = "IN_PROGRESS";  // IN_PROGRESS, COMPLETED, CLAIMED

    @Column(name = "task_date", nullable = false)
    private LocalDate taskDate;

    public boolean isCompleted() {
        return currentProgress >= targetValue && !"CLAIMED".equals(status);
    }

    public boolean isClaimed() {
        return "CLAIMED".equals(status);
    }
}