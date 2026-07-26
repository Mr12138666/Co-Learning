package com.colearning.study;

import com.colearning.common.dto.ApiResponse;
import com.colearning.common.security.SecurityUtils;
import com.colearning.study.dto.request.UpdateCheckinRequest;
import com.colearning.study.dto.response.CheckinResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for daily check-ins and reflections.
 */
@RestController
@RequestMapping("/api/checkins")
@RequiredArgsConstructor
public class CheckinController {

    private final CheckinService checkinService;

    @GetMapping("/today")
    @Operation(summary = "获取或创建今日打卡")
    public ResponseEntity<ApiResponse<CheckinResponse>> getOrCreateToday() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(checkinService.getOrCreateToday(userId)));
    }

    @PutMapping
    @Operation(summary = "更新打卡内容", description = "更新计划、复盘、心情")
    public ResponseEntity<ApiResponse<CheckinResponse>> updateCheckin(
            @Valid @RequestBody UpdateCheckinRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(checkinService.updateCheckin(userId, request)));
    }

    @PostMapping("/complete")
    @Operation(summary = "完成今日打卡")
    public ResponseEntity<ApiResponse<CheckinResponse>> completeCheckin() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(checkinService.completeCheckin(userId)));
    }

    @GetMapping
    @Operation(summary = "按日期查询打卡")
    public ResponseEntity<ApiResponse<CheckinResponse>> getCheckinByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(checkinService.getCheckinByDate(userId, date)));
    }

    @GetMapping("/history")
    @Operation(summary = "查询打卡历史", description = "按日期区间查询打卡记录")
    public ResponseEntity<ApiResponse<List<CheckinResponse>>> getHistory(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(checkinService.getHistory(userId, from, to)));
    }
}
