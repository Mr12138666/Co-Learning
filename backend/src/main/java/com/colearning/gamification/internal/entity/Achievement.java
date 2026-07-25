package com.colearning.gamification.internal.entity;

import com.colearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Achievement definition.
 */
@Entity
@Table(name = "achievements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Achievement extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 500)
    private String description;

    @Column(nullable = false, length = 30)
    private String category;  // STUDY | STREAK | SOCIAL | SPECIAL

    @Column(name = "condition_type", nullable = false, length = 50)
    private String conditionType;  // FOCUS_TOTAL_SEC | STREAK_DAYS | CHECKIN_COUNT | LEVEL

    @Column(name = "condition_value", nullable = false)
    private Integer conditionValue;

    @Column(length = 50)
    private String icon;

    @Column(name = "exp_reward", nullable = false)
    @Builder.Default
    private Integer expReward = 0;

    @Column(name = "token_reward", nullable = false)
    @Builder.Default
    private Integer tokenReward = 0;
}
