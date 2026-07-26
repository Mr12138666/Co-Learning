package com.colearning.user.dto.response;

public record PublicUserProfileResponse(
        Long userId,
        String displayName,
        String avatarUrl,
        String bio
) {}
