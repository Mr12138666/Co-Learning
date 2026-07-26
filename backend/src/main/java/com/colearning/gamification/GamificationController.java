package com.colearning.gamification;

import com.colearning.common.dto.ApiResponse;
import com.colearning.common.security.SecurityUtils;
import com.colearning.gamification.dto.request.RenamePetRequest;
import com.colearning.gamification.dto.response.AchievementResponse;
import com.colearning.gamification.dto.response.DailyTaskResponse;
import com.colearning.gamification.dto.response.GamificationProfileResponse;
import com.colearning.gamification.dto.response.PetItemResponse;
import com.colearning.gamification.dto.response.PetResponse;
import com.colearning.gamification.dto.response.UserItemResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for gamification: experience, pets, shop, achievements.
 */
@RestController
@RequestMapping("/api/gamification")
@RequiredArgsConstructor
public class GamificationController {

    private final GamificationService gamificationService;
    private final DailyTaskService dailyTaskService;

    // ===== Experience & Tokens =====

    @GetMapping("/profile")
    @Operation(summary = "获取成长信息", description = "经验值、等级、代币余额")
    public ResponseEntity<ApiResponse<GamificationProfileResponse>> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getProfile(userId)));
    }

    // ===== Pet =====

    @GetMapping("/pet")
    @Operation(summary = "获取宠物信息", description = "宠物等级、心情、饱食度")
    public ResponseEntity<ApiResponse<PetResponse>> getPet() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getPet(userId)));
    }

    @PutMapping("/pet")
    @Operation(summary = "重命名宠物")
    public ResponseEntity<ApiResponse<PetResponse>> renamePet(@Valid @RequestBody RenamePetRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.renamePet(userId, request)));
    }

    @PostMapping("/pet/feed/{itemId}")
    @Operation(summary = "喂食宠物", description = "使用道具喂养宠物，恢复饱食度")
    public ResponseEntity<ApiResponse<PetResponse>> feedPet(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.feedPet(userId, itemId)));
    }

    @PostMapping("/pet/interact/{itemId}")
    @Operation(summary = "与宠物互动", description = "使用玩具道具，恢复心情值")
    public ResponseEntity<ApiResponse<PetResponse>> interactPet(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.interactPet(userId, itemId)));
    }

    // ===== Shop & Items =====

    @GetMapping("/shop")
    @Operation(summary = "获取道具商店", description = "所有可购买的宠物道具")
    public ResponseEntity<ApiResponse<List<PetItemResponse>>> getShopItems() {
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getShopItems()));
    }

    @GetMapping("/inventory")
    @Operation(summary = "获取我的道具", description = "用户已购买的道具列表")
    public ResponseEntity<ApiResponse<List<UserItemResponse>>> getInventory() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getInventory(userId)));
    }

    @PostMapping("/shop/buy/{itemId}")
    @Operation(summary = "购买道具", description = "用代币购买道具")
    public ResponseEntity<ApiResponse<Void>> purchaseItem(@PathVariable Long itemId) {
        Long userId = SecurityUtils.getCurrentUserId();
        gamificationService.purchaseItem(userId, itemId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    // ===== Achievements =====

    @GetMapping("/achievements")
    @Operation(summary = "获取成就列表", description = "所有成就及解锁状态")
    public ResponseEntity<ApiResponse<List<AchievementResponse>>> getAchievements() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(gamificationService.getAchievements(userId)));
    }

    // ===== Daily Tasks =====

    @GetMapping("/tasks")
    @Operation(summary = "获取今日任务", description = "获取今日的日常任务列表")
    public ResponseEntity<ApiResponse<List<DailyTaskResponse>>> getTodayTasks() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(dailyTaskService.getTodayTasks(userId)));
    }

    @PostMapping("/tasks/{taskId}/claim")
    @Operation(summary = "领取任务奖励", description = "领取已完成任务的代币奖励")
    public ResponseEntity<ApiResponse<DailyTaskResponse>> claimTaskReward(@PathVariable Long taskId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(dailyTaskService.claimReward(userId, taskId)));
    }
}
