package com.colearning.user.dto.response;

import java.time.Instant;

public record BlockedUserResponse(
        Long blockId,
        Long blockedUserId,
        String displayName,
        String avatarUrl,
        Instant blockedAt
) {}
