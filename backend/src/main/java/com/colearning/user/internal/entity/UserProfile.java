package com.colearning.user.internal.entity;

import com.colearning.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;  // Same as users.id (1:1)

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Column(length = 500)
    private String bio;

    @Column(name = "privacy_level", nullable = false)
    @Builder.Default
    private String privacyLevel = "PUBLIC";

    @Column(name = "notif_email_enabled", nullable = false)
    @Builder.Default
    private Boolean notifEmailEnabled = true;

    @Column(name = "notif_push_enabled", nullable = false)
    @Builder.Default
    private Boolean notifPushEnabled = true;

    @Column(nullable = false)
    @Builder.Default
    private String timezone = "Asia/Shanghai";

    @Column(name = "daily_focus_goal_minutes", nullable = false)
    @Builder.Default
    private Integer dailyFocusGoalMinutes = 120;
}
