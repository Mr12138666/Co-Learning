package com.colearning.user;

import com.colearning.user.dto.request.UpdateProfileRequest;
import com.colearning.user.dto.request.UpdateSettingsRequest;
import com.colearning.user.dto.response.BlockedUserResponse;
import com.colearning.user.dto.response.PublicUserProfileResponse;
import com.colearning.user.dto.response.UserProfileResponse;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * Service for managing user profiles, avatar, settings, and block relationships.
 */
public interface UserService {

    UserProfileResponse getProfile(Long userId);

    PublicUserProfileResponse getPublicProfile(Long viewerId, Long targetUserId);

    UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request);

    String uploadAvatar(Long userId, MultipartFile file);

    UserProfileResponse updateSettings(Long userId, UpdateSettingsRequest request);

    // ===== Block management =====

    void blockUser(Long blockerId, Long blockedId);

    void unblockUser(Long blockerId, Long blockedId);

    List<BlockedUserResponse> listBlockedUsers(Long userId);

    boolean isBlocked(Long blockerId, Long blockedId);
}
