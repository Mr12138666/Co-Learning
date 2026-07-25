package com.colearning.room.dto.request;

public record MuteMemberRequest(
    Long durationMinutes,  // null = permanent, otherwise mute for N minutes
    String reason
) {}
