package com.colearning.auth.dto.response;

import java.time.Instant;

/**
 * Response containing JWT tokens after login/registration/refresh.
 * The accessToken goes in memory (frontend), the refreshToken goes in HttpOnly cookie.
 */
public record TokenResponse(
        String accessToken,
        long accessTokenExpiresIn,
        Instant accessTokenExpiresAt,
        Long userId,
        String email,
        String role,
        boolean emailVerified,
        String displayName,
        String avatarUrl
) {}
