package com.colearning.common.exception;

import java.util.List;

/**
 * Field-level validation error detail.
 */
public record ValidationError(
        String field,
        String message,
        Object rejectedValue
) {
    public static List<ValidationError> of(String field, String message, Object rejectedValue) {
        return List.of(new ValidationError(field, message, rejectedValue));
    }
}
