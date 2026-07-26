package com.colearning.journal;

import com.colearning.common.dto.ApiResponse;
import com.colearning.common.dto.PageResponse;
import com.colearning.common.security.SecurityUtils;
import com.colearning.journal.dto.request.CreateJournalRequest;
import com.colearning.journal.dto.request.UpdateJournalRequest;
import com.colearning.journal.dto.response.JournalResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for journal management.
 * Supports draft/publish lifecycle, soft delete, and visibility levels.
 */
@RestController
@RequestMapping("/api/journals")
@RequiredArgsConstructor
public class JournalController {

    private final JournalService journalService;

    @PostMapping
    @Operation(summary = "创建日志", description = "默认为 DRAFT + PRIVATE")
    public ResponseEntity<ApiResponse<JournalResponse>> create(
            @Valid @RequestBody CreateJournalRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(journalService.create(userId, request)));
    }

    @GetMapping("/{journalId}")
    @Operation(summary = "获取日志详情")
    public ResponseEntity<ApiResponse<JournalResponse>> getById(@PathVariable Long journalId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(journalService.getById(userId, journalId)));
    }

    @PutMapping("/{journalId}")
    @Operation(summary = "更新日志")
    public ResponseEntity<ApiResponse<JournalResponse>> update(
            @PathVariable Long journalId,
            @Valid @RequestBody UpdateJournalRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(journalService.update(userId, journalId, request)));
    }

    @DeleteMapping("/{journalId}")
    @Operation(summary = "删除日志(软删除)")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long journalId) {
        Long userId = SecurityUtils.getCurrentUserId();
        journalService.delete(userId, journalId);
        return ResponseEntity.ok(ApiResponse.message("日志已删除"));
    }

    @PostMapping("/{journalId}/publish")
    @Operation(summary = "发布日志")
    public ResponseEntity<ApiResponse<JournalResponse>> publish(@PathVariable Long journalId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(journalService.publish(userId, journalId)));
    }

    @GetMapping
    @Operation(summary = "我的日志列表")
    public ResponseEntity<ApiResponse<PageResponse<JournalResponse>>> listMyJournals(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                journalService.listMyJournals(userId, status, page, size)));
    }

    @GetMapping("/public")
    @Operation(summary = "公开日志广场")
    public ResponseEntity<ApiResponse<PageResponse<JournalResponse>>> listPublicJournals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.ok(
                journalService.listPublicJournals(page, size)));
    }

    @GetMapping("/public/{journalId}")
    @Operation(summary = "获取公开日志详情")
    public ResponseEntity<ApiResponse<JournalResponse>> getPublicJournalById(
            @PathVariable Long journalId) {
        return ResponseEntity.ok(ApiResponse.ok(
                journalService.getPublicJournalById(journalId)));
    }

    @GetMapping("/users/{targetUserId}")
    @Operation(summary = "查看指定用户的公开日志")
    public ResponseEntity<ApiResponse<PageResponse<JournalResponse>>> listUserJournals(
            @PathVariable Long targetUserId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long viewerId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                journalService.listUserJournals(viewerId, targetUserId, page, size)));
    }
}
