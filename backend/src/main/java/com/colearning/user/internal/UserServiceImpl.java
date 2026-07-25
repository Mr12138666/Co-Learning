package com.colearning.user.internal;

import com.colearning.auth.internal.entity.User;
import com.colearning.auth.internal.repository.UserRepository;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.common.storage.StorageService;
import com.colearning.user.UserService;
import com.colearning.user.dto.request.UpdateProfileRequest;
import com.colearning.user.dto.request.UpdateSettingsRequest;
import com.colearning.user.dto.response.BlockedUserResponse;
import com.colearning.user.dto.response.UserProfileResponse;
import com.colearning.user.internal.entity.UserBlock;
import com.colearning.user.internal.entity.UserProfile;
import com.colearning.user.internal.repository.UserBlockRepository;
import com.colearning.user.internal.repository.UserProfileRepository;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

/**
 * Implementation of {@link UserService}.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserProfileRepository profileRepository;
    private final UserBlockRepository blockRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Value("${app.storage.bucket-avatars}")
    private String avatarBucket;

    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {
        UserProfile profile = getProfileOrThrow(userId);
        User user = getUserOrThrow(userId);
        return toResponse(user, profile);
    }

    @Override
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        UserProfile profile = getProfileOrThrow(userId);
        if (request.displayName() != null && !request.displayName().isBlank()) {
            profile.setDisplayName(request.displayName());
        }
        if (request.bio() != null) {
            profile.setBio(request.bio().isBlank() ? null : request.bio());
        }
        profileRepository.save(profile);

        User user = getUserOrThrow(userId);
        return toResponse(user, profile);
    }

    @Override
    public String uploadAvatar(Long userId, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "File is empty");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "Avatar must be under 2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "File must be an image");
        }

        String ext = switch (contentType) {
            case "image/png" -> ".png";
            case "image/jpeg" -> ".jpg";
            case "image/webp" -> ".webp";
            case "image/gif" -> ".gif";
            default -> ".png";
        };
        String objectKey = userId + "/" + UUID.randomUUID() + ext;

        try {
            String url = storageService.upload(avatarBucket, objectKey, file.getBytes(), contentType);
            UserProfile profile = getProfileOrThrow(userId);
            // Delete old avatar from storage if it was uploaded to MinIO
            if (profile.getAvatarUrl() != null && profile.getAvatarUrl().contains(avatarBucket)) {
                String oldKey = profile.getAvatarUrl().substring(
                        profile.getAvatarUrl().indexOf(avatarBucket) + avatarBucket.length() + 1);
                storageService.delete(avatarBucket, oldKey);
            }
            profile.setAvatarUrl(url);
            profileRepository.save(profile);
            log.info("Avatar uploaded: userId={}, url={}", userId, url);
            return url;
        } catch (IOException e) {
            log.error("Failed to read avatar file: userId={}", userId, e);
            throw BusinessException.of(ErrorCode.INTERNAL_ERROR, "Failed to process avatar file");
        }
    }

    @Override
    public UserProfileResponse updateSettings(Long userId, UpdateSettingsRequest request) {
        UserProfile profile = getProfileOrThrow(userId);
        if (request.privacyLevel() != null) {
            profile.setPrivacyLevel(request.privacyLevel());
        }
        if (request.notifEmailEnabled() != null) {
            profile.setNotifEmailEnabled(request.notifEmailEnabled());
        }
        if (request.notifPushEnabled() != null) {
            profile.setNotifPushEnabled(request.notifPushEnabled());
        }
        if (request.timezone() != null && !request.timezone().isBlank()) {
            profile.setTimezone(request.timezone());
        }
        profileRepository.save(profile);

        User user = getUserOrThrow(userId);
        return toResponse(user, profile);
    }

    // ===== Block Management =====

    @Override
    public void blockUser(Long blockerId, Long blockedId) {
        if (blockerId.equals(blockedId)) {
            throw BusinessException.of(ErrorCode.BAD_REQUEST, "Cannot block yourself");
        }
        if (blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw BusinessException.of(ErrorCode.USER_ALREADY_BLOCKED);
        }
        // Verify target user exists
        if (!userRepository.existsById(blockedId)) {
            throw BusinessException.of(ErrorCode.USER_NOT_FOUND);
        }

        UserBlock block = UserBlock.builder()
                .blockerId(blockerId)
                .blockedId(blockedId)
                .createdAt(Instant.now())
                .build();
        blockRepository.save(block);
        log.info("User blocked: {} -> {}", blockerId, blockedId);
    }

    @Override
    public void unblockUser(Long blockerId, Long blockedId) {
        if (!blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId)) {
            throw BusinessException.of(ErrorCode.USER_NOT_BLOCKED);
        }
        blockRepository.deleteByBlockerIdAndBlockedId(blockerId, blockedId);
        log.info("User unblocked: {} -> {}", blockerId, blockedId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BlockedUserResponse> listBlockedUsers(Long userId) {
        return blockRepository.findByBlockerId(userId).stream()
                .map(block -> {
                    UserProfile blockedProfile = profileRepository.findById(block.getBlockedId()).orElse(null);
                    return new BlockedUserResponse(
                            block.getId(),
                            block.getBlockedId(),
                            blockedProfile != null ? blockedProfile.getDisplayName() : "Unknown",
                            blockedProfile != null ? blockedProfile.getAvatarUrl() : null,
                            block.getCreatedAt()
                    );
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isBlocked(Long blockerId, Long blockedId) {
        return blockRepository.existsByBlockerIdAndBlockedId(blockerId, blockedId);
    }

    // ===== Helpers =====

    private UserProfile getProfileOrThrow(Long userId) {
        return profileRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_PROFILE_NOT_FOUND));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.USER_NOT_FOUND));
    }

    private UserProfileResponse toResponse(User user, UserProfile profile) {
        return new UserProfileResponse(
                user.getId(),
                user.getEmail(),
                profile.getDisplayName(),
                profile.getAvatarUrl(),
                profile.getBio(),
                profile.getPrivacyLevel(),
                profile.getNotifEmailEnabled(),
                profile.getNotifPushEnabled(),
                profile.getTimezone(),
                user.getRole(),
                user.getEmailVerified(),
                profile.getCreatedAt()
        );
    }
}
