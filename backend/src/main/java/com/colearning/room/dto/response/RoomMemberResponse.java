package com.colearning.room.dto.response;

import java.time.Instant;

public record RoomMemberResponse(
    Long memberId,
    Long userId,
    String displayName,
    String avatarUrl,
    String role,          // OWNER | ADMIN | MEMBER
    Boolean isMuted,
    Instant mutedUntil,
    Instant joinedAt,
    Boolean isOnline,
    String focusStatus,   // STUDYING | PAUSED | IDLE | null
    Long focusElapsedSeconds,
    String focusTaskTitle
) {}
