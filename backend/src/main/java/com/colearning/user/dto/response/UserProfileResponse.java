package com.colearning.user.dto.response;

import java.time.Instant;

public record UserProfileResponse(
        Long userId,
        String email,
        String displayName,
        String avatarUrl,
        String bio,
        String privacyLevel,
        Boolean notifEmailEnabled,
        Boolean notifPushEnabled,
        String timezone,
        String role,
        boolean emailVerified,
        Instant createdAt
) {}
