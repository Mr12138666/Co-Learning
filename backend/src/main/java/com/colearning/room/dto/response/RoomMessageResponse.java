package com.colearning.room.dto.response;

import java.time.Instant;

public record RoomMessageResponse(
    Long id,
    Long roomId,
    Long userId,
    String displayName,
    String avatarUrl,
    String content,
    String messageType,   // TEXT | SYSTEM | FOCUS_STATUS
    String focusStatus,
    Instant createdAt
) {}
