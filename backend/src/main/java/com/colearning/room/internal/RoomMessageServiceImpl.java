package com.colearning.room.internal;

import com.colearning.common.dto.PageResponse;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.room.RoomMessageService;
import com.colearning.room.dto.request.SendRoomMessageRequest;
import com.colearning.room.dto.response.RoomMessageResponse;
import com.colearning.room.internal.entity.RoomMember;
import com.colearning.room.internal.entity.RoomMessage;
import com.colearning.room.internal.repository.RoomMemberRepository;
import com.colearning.room.internal.repository.RoomMessageRepository;
import com.colearning.room.internal.repository.RoomRepository;
import com.colearning.user.internal.entity.UserProfile;
import com.colearning.user.internal.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomMessageServiceImpl implements RoomMessageService {

    private final RoomMessageRepository roomMessageRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomRepository roomRepository;
    private final UserProfileRepository userProfileRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public RoomMessageResponse sendMessage(Long userId, Long roomId, SendRoomMessageRequest request) {
        // Validate room exists
        roomRepository.findById(roomId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ROOM_NOT_FOUND));

        // Validate membership
        RoomMember member = roomMemberRepository.findActiveMember(roomId, userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ROOM_NOT_MEMBER));

        // Check mute status
        if (member.isMuted()) {
            throw BusinessException.of(ErrorCode.ROOM_MUTED);
        }

        String messageType = request.messageType() != null ? request.messageType() : "TEXT";
        if (!"TEXT".equals(messageType) && !"FOCUS_STATUS".equals(messageType)) {
            messageType = "TEXT";
        }

        RoomMessage message = RoomMessage.builder()
                .roomId(roomId)
                .userId(userId)
                .content(request.content())
                .messageType(messageType)
                .focusStatus(request.focusStatus())
                .build();

        message = roomMessageRepository.save(message);

        // Build response with user profile
        UserProfile profile = userProfileRepository.findById(userId).orElse(null);
        RoomMessageResponse response = toMessageResponse(message, profile);

        // Broadcast to room subscribers via WebSocket
        String destination = "/topic/rooms/" + roomId + "/messages";
        messagingTemplate.convertAndSend(destination, response);

        log.debug("Message sent: roomId={}, userId={}, type={}", roomId, userId, messageType);
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomMessageResponse> listMessages(Long roomId, Long userId, Pageable pageable) {
        // Validate membership
        roomMemberRepository.findActiveMember(roomId, userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ROOM_NOT_MEMBER));

        Page<RoomMessage> messages = roomMessageRepository.findByRoomId(roomId, pageable);

        Set<Long> userIds = messages.getContent().stream()
                .map(RoomMessage::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserProfile> profileMap = loadUserProfiles(userIds);

        List<RoomMessageResponse> responses = messages.getContent().stream()
                .map(msg -> toMessageResponse(msg, profileMap.get(msg.getUserId())))
                .toList();

        return PageResponse.of(responses, messages.getNumber(), messages.getSize(),
                messages.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomMessageResponse> getRecentMessages(Long roomId, int limit) {
        List<RoomMessage> messages = roomMessageRepository.findRecentMessages(roomId, limit);

        Set<Long> userIds = messages.stream()
                .map(RoomMessage::getUserId)
                .collect(Collectors.toSet());
        Map<Long, UserProfile> profileMap = loadUserProfiles(userIds);

        // Reverse to oldest-first
        java.util.Collections.reverse(messages);

        return messages.stream()
                .map(msg -> toMessageResponse(msg, profileMap.get(msg.getUserId())))
                .toList();
    }

    // ===== Private helpers =====

    private Map<Long, UserProfile> loadUserProfiles(Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        return userProfileRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserProfile::getUserId, p -> p, (a, b) -> a));
    }

    private RoomMessageResponse toMessageResponse(RoomMessage message, UserProfile profile) {
        String displayName = profile != null ? profile.getDisplayName() : "User" + message.getUserId();
        String avatarUrl = profile != null ? profile.getAvatarUrl() : null;

        return new RoomMessageResponse(
                message.getId(),
                message.getRoomId(),
                message.getUserId(),
                displayName,
                avatarUrl,
                message.getContent(),
                message.getMessageType(),
                message.getFocusStatus(),
                message.getCreatedAt()
        );
    }
}
