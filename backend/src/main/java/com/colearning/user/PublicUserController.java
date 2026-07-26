package com.colearning.user;

import com.colearning.common.dto.ApiResponse;
import com.colearning.common.security.SecurityUtils;
import com.colearning.user.dto.response.PublicUserProfileResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class PublicUserController {

    private final UserService userService;

    @GetMapping("/{userId}/profile")
    public ResponseEntity<ApiResponse<PublicUserProfileResponse>> getPublicProfile(
            @PathVariable Long userId) {
        Long viewerId = SecurityUtils.getCurrentUserIdOrNull();
        return ResponseEntity.ok(ApiResponse.ok(userService.getPublicProfile(viewerId, userId)));
    }
}
