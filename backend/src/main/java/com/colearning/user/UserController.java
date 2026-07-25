package com.colearning.user;

import com.colearning.common.dto.ApiResponse;
import com.colearning.common.security.SecurityUtils;
import com.colearning.user.dto.request.UpdateProfileRequest;
import com.colearning.user.dto.request.UpdateSettingsRequest;
import com.colearning.user.dto.response.BlockedUserResponse;
import com.colearning.user.dto.response.UserProfileResponse;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for user profile, settings, and block management.
 * All endpoints require authentication.
 */
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(userId)));
    }

    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.updateProfile(userId, request)));
    }

    @PostMapping("/avatar")
    public ResponseEntity<ApiResponse<String>> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        Long userId = SecurityUtils.getCurrentUserId();
        String url = userService.uploadAvatar(userId, file);
        return ResponseEntity.ok(ApiResponse.ok(url));
    }

    @GetMapping("/settings")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getSettings() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.getProfile(userId)));
    }

    @PutMapping("/settings")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateSettings(
            @Valid @RequestBody UpdateSettingsRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.updateSettings(userId, request)));
    }

    // ===== Block management =====

    @GetMapping("/blocks")
    public ResponseEntity<ApiResponse<List<BlockedUserResponse>>> listBlocks() {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(userService.listBlockedUsers(userId)));
    }

    @PostMapping("/blocks/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> blockUser(@PathVariable Long targetUserId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.blockUser(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.message("User blocked"));
    }

    @DeleteMapping("/blocks/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> unblockUser(@PathVariable Long targetUserId) {
        Long userId = SecurityUtils.getCurrentUserId();
        userService.unblockUser(userId, targetUserId);
        return ResponseEntity.ok(ApiResponse.message("User unblocked"));
    }
}
