package com.colearning.gamification.internal.entity;

import com.colearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * User's companion pet.
 * Each user has exactly one pet (unique on user_id).
 */
@Entity
@Table(name = "pets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Pet extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String species = "CAT";

    @Column(nullable = false)
    @Builder.Default
    private Integer level = 1;

    @Column(nullable = false)
    @Builder.Default
    private Integer exp = 0;

    /** 0-100, decreases over time */
    @Column(nullable = false)
    @Builder.Default
    private Integer mood = 100;

    /** 0-100, decreases over time */
    @Column(nullable = false)
    @Builder.Default
    private Integer hunger = 100;

    @Column(name = "last_fed_at")
    private Instant lastFedAt;

    @Column(name = "last_interacted_at")
    private Instant lastInteractedAt;
}
