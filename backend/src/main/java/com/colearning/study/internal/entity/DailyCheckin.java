package com.colearning.study.internal.entity;

import com.colearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_checkins", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "checkin_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyCheckin extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "checkin_date", nullable = false)
    private LocalDate checkinDate;

    @Column(name = "plan_text", length = 1000)
    private String planText;

    @Column(name = "reflection_text", length = 2000)
    private String reflectionText;

    @Column
    private Short mood;  // 1-5

    @Column(name = "focus_total_sec", nullable = false)
    @Builder.Default
    private Integer focusTotalSec = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean completed = false;

    @Column(columnDefinition = "jsonb")
    @Builder.Default
    private String images = "[]";
}
