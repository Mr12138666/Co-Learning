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
@Table(name = "room_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomMessage extends SoftDeletableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(name = "message_type", nullable = false)
    @Builder.Default
    private String messageType = "TEXT";  // TEXT | SYSTEM | FOCUS_STATUS

    @Column(name = "focus_status")
    private String focusStatus;  // STUDYING | PAUSED | IDLE (for FOCUS_STATUS type)

    // --- Helpers ---

    public boolean isText() {
        return "TEXT".equals(messageType);
    }

    public boolean isSystem() {
        return "SYSTEM".equals(messageType);
    }

    public boolean isFocusStatus() {
        return "FOCUS_STATUS".equals(messageType);
    }
}
