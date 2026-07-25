package com.colearning.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * Application error codes mapped to HTTP status codes.
 * Format: MODULE-ERROR_TYPE (e.g., AUTH-001, STUDY-001).
 */
@Getter
public enum ErrorCode {

    // ===== Generic (0xx) =====
    INTERNAL_ERROR("GEN-000", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    BAD_REQUEST("GEN-001", "Bad request", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR("GEN-002", "Validation failed", HttpStatus.BAD_REQUEST),
    NOT_FOUND("GEN-003", "Resource not found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("GEN-004", "Method not allowed", HttpStatus.METHOD_NOT_ALLOWED),
    FORBIDDEN("GEN-005", "Access forbidden", HttpStatus.FORBIDDEN),
    UNAUTHORIZED("GEN-006", "Unauthorized", HttpStatus.UNAUTHORIZED),
    TOO_MANY_REQUESTS("GEN-007", "Too many requests", HttpStatus.TOO_MANY_REQUESTS),
    CONFLICT("GEN-008", "Resource conflict", HttpStatus.CONFLICT),

    // ===== Auth (1xx) =====
    AUTH_INVALID_CREDENTIALS("AUTH-001", "Invalid email or password", HttpStatus.UNAUTHORIZED),
    AUTH_EMAIL_NOT_VERIFIED("AUTH-002", "Email not verified", HttpStatus.FORBIDDEN),
    AUTH_TOKEN_EXPIRED("AUTH-003", "Token expired", HttpStatus.UNAUTHORIZED),
    AUTH_TOKEN_INVALID("AUTH-004", "Invalid token", HttpStatus.UNAUTHORIZED),
    AUTH_REFRESH_TOKEN_INVALID("AUTH-005", "Invalid or expired refresh token", HttpStatus.UNAUTHORIZED),
    AUTH_EMAIL_ALREADY_EXISTS("AUTH-006", "Email already registered", HttpStatus.CONFLICT),
    AUTH_VERIFICATION_TOKEN_INVALID("AUTH-007", "Invalid or expired verification token", HttpStatus.BAD_REQUEST),
    AUTH_ACCOUNT_LOCKED("AUTH-008", "Account temporarily locked", HttpStatus.LOCKED),
    AUTH_ACCOUNT_SUSPENDED("AUTH-009", "Account suspended", HttpStatus.FORBIDDEN),
    AUTH_PASSWORD_TOO_WEAK("AUTH-010", "Password does not meet security requirements", HttpStatus.BAD_REQUEST),

    // ===== User (2xx) =====
    USER_NOT_FOUND("USER-001", "User not found", HttpStatus.NOT_FOUND),
    USER_PROFILE_NOT_FOUND("USER-002", "User profile not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_BLOCKED("USER-003", "User already blocked", HttpStatus.CONFLICT),
    USER_NOT_BLOCKED("USER-004", "User not in block list", HttpStatus.NOT_FOUND),

    // ===== Study (3xx) =====
    STUDY_SESSION_NOT_FOUND("STUDY-001", "Study session not found", HttpStatus.NOT_FOUND),
    STUDY_SESSION_ALREADY_ACTIVE("STUDY-002", "An active study session already exists", HttpStatus.CONFLICT),
    STUDY_SESSION_NOT_ACTIVE("STUDY-003", "Study session is not active", HttpStatus.CONFLICT),
    STUDY_SESSION_ALREADY_FINISHED("STUDY-004", "Study session already finished", HttpStatus.CONFLICT),
    STUDY_SUBJECT_NOT_FOUND("STUDY-005", "Subject not found", HttpStatus.NOT_FOUND),
    STUDY_TASK_NOT_FOUND("STUDY-006", "Task not found", HttpStatus.NOT_FOUND),
    STUDY_GOAL_NOT_FOUND("STUDY-007", "Exam goal not found", HttpStatus.NOT_FOUND),
    STUDY_INVALID_STATE_TRANSITION("STUDY-008", "Invalid session state transition", HttpStatus.CONFLICT),

    // ===== Journal (4xx) =====
    JOURNAL_NOT_FOUND("JRN-001", "Journal not found", HttpStatus.NOT_FOUND),
    JOURNAL_ALREADY_DELETED("JRN-002", "Journal already deleted", HttpStatus.CONFLICT),
    JOURNAL_ALREADY_PUBLISHED("JRN-003", "Journal already published", HttpStatus.CONFLICT),

    // ===== Room (5xx) =====
    ROOM_NOT_FOUND("ROOM-001", "Room not found", HttpStatus.NOT_FOUND),
    ROOM_FULL("ROOM-002", "Room is full", HttpStatus.CONFLICT),
    ROOM_ALREADY_MEMBER("ROOM-003", "Already a member of this room", HttpStatus.CONFLICT),
    ROOM_NOT_MEMBER("ROOM-004", "Not a member of this room", HttpStatus.FORBIDDEN),
    ROOM_NOT_OWNER("ROOM-005", "Only room owner can perform this action", HttpStatus.FORBIDDEN),
    ROOM_CLOSED("ROOM-006", "Room is closed", HttpStatus.CONFLICT),
    ROOM_MUTED("ROOM-007", "You are muted in this room", HttpStatus.FORBIDDEN),

    // ===== Leaderboard (6xx) =====
    LEADERBOARD_NOT_AVAILABLE("LB-001", "Leaderboard not available", HttpStatus.NOT_FOUND),

    // ===== Gamification (7xx) =====
    PET_NOT_FOUND("PET-001", "Pet not found", HttpStatus.NOT_FOUND),
    INSUFFICIENT_TOKENS("PET-002", "Insufficient tokens", HttpStatus.CONFLICT),
    ITEM_NOT_FOUND("PET-003", "Item not found", HttpStatus.NOT_FOUND),
    ALREADY_OWNED("PET-004", "Item already owned", HttpStatus.CONFLICT),

    // ===== AI (8xx) =====
    AI_SERVICE_UNAVAILABLE("AI-001", "AI service temporarily unavailable", HttpStatus.SERVICE_UNAVAILABLE),

    // ===== Moderation (9xx) =====
    REPORT_NOT_FOUND("MOD-001", "Report not found", HttpStatus.NOT_FOUND),
    SENSITIVE_CONTENT("MOD-002", "Content contains sensitive words", HttpStatus.BAD_REQUEST),
    ;

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }
}
