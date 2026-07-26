package com.colearning.common.security;

import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility for accessing the current authenticated user from SecurityContext.
 */
public final class SecurityUtils {

    private SecurityUtils() {}

    public static PrincipalUser getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof PrincipalUser)) {
            throw BusinessException.of(ErrorCode.UNAUTHORIZED);
        }
        return (PrincipalUser) auth.getPrincipal();
    }

    public static Long getCurrentUserId() {
        return getCurrentUser().userId();
    }

    public static Long getCurrentUserIdOrNull() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof PrincipalUser)) {
            return null;
        }
        return ((PrincipalUser) auth.getPrincipal()).userId();
    }

    public static boolean isAuthenticated() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.isAuthenticated()
                && auth.getPrincipal() instanceof PrincipalUser;
    }
}
