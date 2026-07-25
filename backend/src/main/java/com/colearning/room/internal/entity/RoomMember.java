package com.colearning.room.internal.entity;

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

@Entity
@Table(name = "room_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMember extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    @Builder.Default
    private String role = "MEMBER";  // OWNER | ADMIN | MEMBER

    @Column(name = "muted_until")
    private Instant mutedUntil;

    @Column(name = "joined_at")
    private Instant joinedAt;

    @Column(name = "left_at")
    private Instant leftAt;

    // --- Helpers ---

    public boolean isOwner() {
        return "OWNER".equals(role);
    }

    public boolean isAdmin() {
        return "OWNER".equals(role) || "ADMIN".equals(role);
    }

    public boolean isMuted() {
        return mutedUntil != null && mutedUntil.isAfter(Instant.now());
    }

    public boolean hasLeft() {
        return leftAt != null;
    }
}
