package com.colearning.common.security;

/**
 * Authenticated user principal stored in Spring Security context.
 */
public record PrincipalUser(
        Long userId,
        String email,
        String role,
        boolean emailVerified
) {
    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}
