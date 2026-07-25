package com.colearning.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Unified API response wrapper.
 *
 * @param code     business code (0 = success, non-zero = error)
 * @param message  human-readable message
 * @param data     response payload (null if error)
 * @param traceId  request trace ID for debugging
 * @param timestamp response generation time
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        int code,
        String message,
        T data,
        String traceId,
        Instant timestamp
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(0, "success", data, null, Instant.now());
    }

    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(0, message, data, null, Instant.now());
    }

    public static <T> ApiResponse<T> ok() {
        return new ApiResponse<>(0, "success", null, null, Instant.now());
    }

    public static ApiResponse<Void> message(String message) {
        return new ApiResponse<>(0, message, null, null, Instant.now());
    }

    public static <T> ApiResponse<T> error(int code, String message) {
        return new ApiResponse<>(code, message, null, null, Instant.now());
    }
}
