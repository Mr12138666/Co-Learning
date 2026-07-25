package com.colearning.study;

import com.colearning.common.dto.ApiResponse;
import com.colearning.common.security.SecurityUtils;
import com.colearning.study.dto.request.StartFocusRequest;
import com.colearning.study.dto.response.ActiveFocusResponse;
import com.colearning.study.dto.response.FocusSessionResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for focus session management.
 * Server-authoritative timing: all state transitions are validated server-side.
 */
@RestController
@RequestMapping("/api/focus-sessions")
@RequiredArgsConstructor
public class FocusSessionController {

    private final FocusSessionService focusSessionService;

    @PostMapping
    @Operation(summary = "开始专注会话", description = "幂等：相同 clientRequestId 返回已有会话")
    public ResponseEntity<ApiResponse<FocusSessionResponse>> start(
            @Valid @RequestBody StartFocusRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(focusSessionService.start(userId, request)));
    }

    @GetMapping("/active")
    @Operation(summary = "获取当前活跃会话", description = "返回 ACTIVE 或 PAUSED 状态的会话")
    public ResponseEntity<ApiResponse<ActiveFocusResponse>> getActive() {
        Long userId = SecurityUtils.getCurrentUserId();
        return focusSessionService.getActiveSession(userId)
                .map(resp -> ResponseEntity.ok(ApiResponse.ok(resp)))
                .orElseGet(() -> ResponseEntity.ok(ApiResponse.ok(null)));
    }

    @GetMapping("/{sessionId}")
    @Operation(summary = "获取会话详情")
    public ResponseEntity<ApiResponse<FocusSessionResponse>> getSession(
            @PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(focusSessionService.getSession(userId, sessionId)));
    }

    @PostMapping("/{sessionId}/pause")
    @Operation(summary = "暂停会话")
    public ResponseEntity<ApiResponse<FocusSessionResponse>> pause(
            @PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(focusSessionService.pause(userId, sessionId)));
    }

    @PostMapping("/{sessionId}/resume")
    @Operation(summary = "恢复会话")
    public ResponseEntity<ApiResponse<FocusSessionResponse>> resume(
            @PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(focusSessionService.resume(userId, sessionId)));
    }

    @PostMapping("/{sessionId}/finish")
    @Operation(summary = "结束会话", description = "计算有效时间并发布事件")
    public ResponseEntity<ApiResponse<FocusSessionResponse>> finish(
            @PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(focusSessionService.finish(userId, sessionId)));
    }

    @PostMapping("/{sessionId}/abort")
    @Operation(summary = "放弃会话", description = "不计入有效时间")
    public ResponseEntity<ApiResponse<FocusSessionResponse>> abort(
            @PathVariable Long sessionId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(focusSessionService.abort(userId, sessionId)));
    }
}
