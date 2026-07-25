package com.colearning.common.ratelimit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for rate-limiting API endpoints.
 * Uses Redis to track request counts per key within a time window.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Rate limit key prefix, e.g., "login", "email".
     * The full key is: rate:{key}:{identifier}
     */
    String key();

    /**
     * Maximum number of requests allowed within the window.
     */
    int limit() default 5;

    /**
     * Time window in seconds.
     */
    int window() default 60;

    /**
     * SpEL expression for the identifier (evaluated against method args).
     * Defaults to client IP address.
     * Examples: "#request.email", "#userId", "T(com.colearning.common.security.SecurityUtils).getCurrentUserId()"
     */
    String identifier() default "";
}
