package com.colearning.room.internal.entity;

import com.colearning.common.entity.SoftDeletableEntity;
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

@Entity
@Table(name = "rooms")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "max_members")
    @Builder.Default
    private Integer maxMembers = 20;

    @Column(nullable = false)
    @Builder.Default
    private String visibility = "PUBLIC";  // PUBLIC | PRIVATE

    @Column(nullable = false)
    @Builder.Default
    private String status = "ACTIVE";  // ACTIVE | CLOSED

    @Column(name = "password_hash")
    private String passwordHash;

    @Column(length = 200)
    private String topic;

    // --- Helpers ---

    public boolean isPublic() {
        return "PUBLIC".equals(visibility);
    }

    public boolean isPrivate() {
        return "PRIVATE".equals(visibility);
    }

    public boolean isActive() {
        return "ACTIVE".equals(status);
    }

    public boolean isClosed() {
        return "CLOSED".equals(status);
    }

    public boolean hasPassword() {
        return passwordHash != null && !passwordHash.isBlank();
    }
}
