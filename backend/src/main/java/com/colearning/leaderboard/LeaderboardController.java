package com.colearning.leaderboard;

import com.colearning.common.dto.ApiResponse;
import com.colearning.common.security.SecurityUtils;
import com.colearning.leaderboard.dto.response.LeaderboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for leaderboard queries.
 */
@RestController
@RequestMapping("/api/leaderboard")
@RequiredArgsConstructor
public class LeaderboardController {

    private final LeaderboardService leaderboardService;

    @GetMapping
    @Operation(summary = "获取排行榜", description = "支持 daily/weekly/alltime 三种类型")
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getLeaderboard(
            @RequestParam(defaultValue = "daily") String type,
            @RequestParam(defaultValue = "20") int limit
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        limit = Math.min(limit, 100);  // cap at 100
        return ResponseEntity.ok(ApiResponse.ok(leaderboardService.getLeaderboard(type, limit, userId)));
    }

    @GetMapping("/me")
    @Operation(summary = "获取我的排名", description = "获取当前用户在指定排行榜中的排名")
    public ResponseEntity<ApiResponse<LeaderboardResponse>> getMyRank(
            @RequestParam(defaultValue = "daily") String type
    ) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(leaderboardService.getMyRank(type, userId)));
    }

    @GetMapping("/sync")
    @Operation(summary = "同步排行榜", description = "从数据库同步排行榜数据（用于修复Redis数据丢失）")
    public ResponseEntity<ApiResponse<Void>> syncLeaderboard() {
        leaderboardService.syncFromDatabase();
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
