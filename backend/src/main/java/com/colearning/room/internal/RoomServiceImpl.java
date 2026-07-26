package com.colearning.room.internal;

import com.colearning.common.dto.PageResponse;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.colearning.common.security.Argon2PasswordEncoder;
import com.colearning.room.PresenceService;
import com.colearning.room.RoomService;
import com.colearning.room.dto.request.CreateRoomRequest;
import com.colearning.room.dto.request.JoinRoomRequest;
import com.colearning.room.dto.request.MuteMemberRequest;
import com.colearning.room.dto.request.UpdateRoomRequest;
import com.colearning.room.dto.response.RoomMemberResponse;
import com.colearning.room.dto.response.RoomMessageResponse;
import com.colearning.room.dto.response.RoomResponse;
import com.colearning.room.dto.response.RoomStateResponse;
import com.colearning.room.internal.entity.Room;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final RoomMessageRepository roomMessageRepository;
    private final UserProfileRepository userProfileRepository;
    private final Argon2PasswordEncoder passwordEncoder;
    private final PresenceService presenceService;

    // ===== Room CRUD =====

    @Override
    public RoomResponse createRoom(Long userId, CreateRoomRequest request) {
        String visibility = request.visibility() != null ? request.visibility() : "PUBLIC";
        String passwordHash = null;

        if ("PRIVATE".equals(visibility)) {
            if (request.password() == null || request.password().isBlank()) {
                throw BusinessException.of(ErrorCode.ROOM_PASSWORD_REQUIRED);
            }
            passwordHash = passwordEncoder.encode(request.password());
        }

        Room room = Room.builder()
                .ownerId(userId)
                .name(request.name())
                .description(request.description())
                .maxMembers(request.maxMembers() != null ? request.maxMembers() : 20)
                .visibility(visibility)
                .status("ACTIVE")
                .passwordHash(passwordHash)
                .topic(request.topic())
                .build();

        room = roomRepository.save(room);

        // Create owner membership
        RoomMember member = RoomMember.builder()
                .roomId(room.getId())
                .userId(userId)
                .role("OWNER")
                .joinedAt(Instant.now())
                .build();
        roomMemberRepository.save(member);

        return toRoomResponse(room, userId, 1L, true);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<RoomResponse> listPublicRooms(Long userId, Pageable pageable) {
        Page<Room> rooms = roomRepository.findPublicRooms(pageable);

        List<RoomResponse> responses = rooms.getContent().stream()
                .map(room -> {
                    long memberCount = roomMemberRepository.countActiveMembers(room.getId());
                    boolean isMember = roomMemberRepository
                            .findActiveMember(room.getId(), userId).isPresent();
                    return toRoomResponse(room, userId, memberCount, isMember);
                })
                .toList();

        return PageResponse.of(responses, rooms.getNumber(), rooms.getSize(), rooms.getTotalElements());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse getRoom(Long roomId, Long userId) {
        Room room = findRoomById(roomId);
        long memberCount = roomMemberRepository.countActiveMembers(roomId);
        boolean isMember = roomMemberRepository.findActiveMember(roomId, userId).isPresent();
        return toRoomResponse(room, userId, memberCount, isMember);
    }

    @Override
    public RoomResponse updateRoom(Long userId, Long roomId, UpdateRoomRequest request) {
        Room room = findRoomById(roomId);
        assertOwner(room, userId);

        if (request.name() != null) room.setName(request.name());
        if (request.description() != null) room.setDescription(request.description());
        if (request.maxMembers() != null) room.setMaxMembers(request.maxMembers());
        if (request.status() != null) room.setStatus(request.status());
        if (request.topic() != null) room.setTopic(request.topic());
        if (request.password() != null) {
            room.setPasswordHash(passwordEncoder.encode(request.password()));
            room.setVisibility("PRIVATE");
        }

        room = roomRepository.save(room);
        long memberCount = roomMemberRepository.countActiveMembers(roomId);
        return toRoomResponse(room, userId, memberCount, true);
    }

    @Override
    public void deleteRoom(Long userId, Long roomId) {
        Room room = findRoomById(roomId);
        assertOwner(room, userId);

        room.softDelete();
        room.setStatus("CLOSED");
        roomRepository.save(room);

        // Mark all members as left
        List<RoomMember> members = roomMemberRepository.findActiveMembers(roomId);
        for (RoomMember m : members) {
            m.setLeftAt(Instant.now());
        }
        roomMemberRepository.saveAll(members);

        log.info("Room deleted: roomId={}, ownerId={}", roomId, userId);
    }

    // ===== Membership =====

    @Override
    public RoomMemberResponse joinRoom(Long userId, Long roomId, JoinRoomRequest request) {
        Room room = findRoomById(roomId);

        if (room.isClosed()) {
            throw BusinessException.of(ErrorCode.ROOM_CLOSED);
        }

        // Check capacity
        long currentCount = roomMemberRepository.countActiveMembers(roomId);
        if (currentCount >= room.getMaxMembers()) {
            throw BusinessException.of(ErrorCode.ROOM_FULL);
        }

        // Check password for private rooms
        if (room.isPrivate() && room.hasPassword()) {
            if (request == null || request.password() == null
                    || !passwordEncoder.matches(request.password(), room.getPasswordHash())) {
                throw BusinessException.of(ErrorCode.ROOM_PASSWORD_INCORRECT);
            }
        }

        // Check if already a member
        Optional<RoomMember> existing = roomMemberRepository.findByRoomIdAndUserId(roomId, userId);
        if (existing.isPresent() && !existing.get().hasLeft()) {
            throw BusinessException.of(ErrorCode.ROOM_ALREADY_MEMBER);
        }

        // Rejoin if previously left, otherwise create new
        RoomMember member;
        if (existing.isPresent()) {
            member = existing.get();
            member.setLeftAt(null);
            member.setJoinedAt(Instant.now());
            member.setMutedUntil(null);
        } else {
            member = RoomMember.builder()
                    .roomId(roomId)
                    .userId(userId)
                    .role("MEMBER")
                    .joinedAt(Instant.now())
                    .build();
        }
        member = roomMemberRepository.save(member);

        // Create system message
        createSystemMessage(roomId, userId, "joined the room");

        return toMemberResponse(member, false, null);
    }

    @Override
    public void leaveRoom(Long userId, Long roomId) {
        Room room = findRoomById(roomId);
        RoomMember member = findActiveMember(roomId, userId);

        if (member.isOwner()) {
            throw BusinessException.of(ErrorCode.ROOM_NOT_OWNER,
                    "Owner cannot leave. Transfer ownership or delete the room.");
        }

        member.setLeftAt(Instant.now());
        roomMemberRepository.save(member);

        createSystemMessage(roomId, userId, "left the room");
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomMemberResponse> listMembers(Long roomId, Long userId) {
        Room room = findRoomById(roomId);
        List<RoomMember> members = roomMemberRepository.findActiveMembers(roomId);

        Set<Long> userIds = members.stream().map(RoomMember::getUserId).collect(Collectors.toSet());
        Map<Long, UserProfile> profileMap = loadUserProfiles(userIds);
        Set<Long> onlineUserIds = presenceService.getOnlineUserIds(roomId);
        Map<Long, String> focusStatuses = presenceService.getFocusStatuses(roomId);

        return members.stream()
                .map(m -> {
                    UserProfile profile = profileMap.get(m.getUserId());
                    boolean isOnline = onlineUserIds.contains(m.getUserId());
                    String focusStatus = focusStatuses.get(m.getUserId());
                    return toMemberResponse(m, isOnline, focusStatus, profile);
                })
                .toList();
    }

    // ===== Moderation =====

    @Override
    public void kickMember(Long userId, Long roomId, Long targetUserId) {
        Room room = findRoomById(roomId);
        RoomMember actor = findActiveMember(roomId, userId);
        assertAdmin(actor);

        RoomMember target = findActiveMember(roomId, targetUserId);
        if (target.isOwner()) {
            throw BusinessException.of(ErrorCode.ROOM_NOT_OWNER,
                    "Cannot kick the room owner");
        }

        target.setLeftAt(Instant.now());
        roomMemberRepository.save(target);

        createSystemMessage(roomId, targetUserId, "was kicked from the room");
        log.info("Member kicked: roomId={}, target={}, by={}", roomId, targetUserId, userId);
    }

    @Override
    public void muteMember(Long userId, Long roomId, Long targetUserId, MuteMemberRequest request) {
        Room room = findRoomById(roomId);
        RoomMember actor = findActiveMember(roomId, userId);
        assertAdmin(actor);

        RoomMember target = findActiveMember(roomId, targetUserId);
        if (target.isOwner()) {
            throw BusinessException.of(ErrorCode.ROOM_NOT_OWNER,
                    "Cannot mute the room owner");
        }

        Instant mutedUntil = null;
        if (request.durationMinutes() != null && request.durationMinutes() > 0) {
            mutedUntil = Instant.now().plusSeconds(request.durationMinutes() * 60);
        } else {
            // Permanent mute (100 years)
            mutedUntil = Instant.now().plusSeconds(365L * 100 * 24 * 60 * 60);
        }

        target.setMutedUntil(mutedUntil);
        roomMemberRepository.save(target);

        log.info("Member muted: roomId={}, target={}, by={}, until={}",
                roomId, targetUserId, userId, mutedUntil);
    }

    // ===== Room State (reconnect snapshot) =====

    @Override
    @Transactional(readOnly = true)
    public RoomStateResponse getRoomState(Long roomId, Long userId) {
        Room room = findRoomById(roomId);

        // Validate membership
        roomMemberRepository.findActiveMember(roomId, userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ROOM_NOT_MEMBER));

        List<RoomMember> members = roomMemberRepository.findActiveMembers(roomId);
        Set<Long> userIds = members.stream().map(RoomMember::getUserId).collect(Collectors.toSet());
        Map<Long, UserProfile> profileMap = loadUserProfiles(userIds);

        Set<Long> onlineUserIds = presenceService.getOnlineUserIds(roomId);
        Map<Long, String> focusStatuses = presenceService.getFocusStatuses(roomId);

        List<RoomMemberResponse> memberResponses = members.stream()
                .map(m -> {
                    UserProfile profile = profileMap.get(m.getUserId());
                    boolean isOnline = onlineUserIds.contains(m.getUserId());
                    String focusStatus = focusStatuses.get(m.getUserId());
                    return toMemberResponse(m, isOnline, focusStatus, profile);
                })
                .toList();

        // Get recent messages (last 50, oldest to newest)
        List<RoomMessage> recentMessages = roomMessageRepository.findRecentMessages(roomId, 50);
        Collections.reverse(recentMessages); // oldest first

        Set<Long> messageUserIds = recentMessages.stream()
                .map(RoomMessage::getUserId)
                .collect(Collectors.toSet());
        messageUserIds.addAll(userIds);
        Map<Long, UserProfile> allProfiles = loadUserProfiles(messageUserIds);

        List<RoomMessageResponse> messageResponses = recentMessages.stream()
                .map(msg -> {
                    UserProfile profile = allProfiles.get(msg.getUserId());
                    return toMessageResponse(msg, profile);
                })
                .toList();

        return new RoomStateResponse(
                roomId,
                room.getName(),
                room.getStatus(),
                memberResponses,
                new ArrayList<>(onlineUserIds),
                messageResponses,
                Instant.now()
        );
    }

    // ===== Private helpers =====

    private Room findRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ROOM_NOT_FOUND));
    }

    private RoomMember findActiveMember(Long roomId, Long userId) {
        return roomMemberRepository.findActiveMember(roomId, userId)
                .orElseThrow(() -> BusinessException.of(ErrorCode.ROOM_NOT_MEMBER));
    }

    private void assertOwner(Room room, Long userId) {
        if (!room.getOwnerId().equals(userId)) {
            throw BusinessException.of(ErrorCode.ROOM_NOT_OWNER);
        }
    }

    private void assertAdmin(RoomMember member) {
        if (!member.isAdmin()) {
            throw BusinessException.of(ErrorCode.ROOM_NOT_ADMIN);
        }
    }

    private void createSystemMessage(Long roomId, Long userId, String action) {
        RoomMessage msg = RoomMessage.builder()
                .roomId(roomId)
                .userId(userId)
                .content(action)
                .messageType("SYSTEM")
                .build();
        roomMessageRepository.save(msg);
    }

    private Map<Long, UserProfile> loadUserProfiles(Set<Long> userIds) {
        if (userIds.isEmpty()) return Map.of();
        List<UserProfile> profiles = userProfileRepository.findAllById(userIds);
        return profiles.stream()
                .collect(Collectors.toMap(UserProfile::getUserId, p -> p, (a, b) -> a));
    }

    private RoomResponse toRoomResponse(Room room, Long currentUserId,
                                        long memberCount, boolean isMember) {
        UserProfile ownerProfile = userProfileRepository.findById(room.getOwnerId()).orElse(null);
        String ownerName = ownerProfile != null ? ownerProfile.getDisplayName() : "Unknown";
        String ownerAvatar = ownerProfile != null ? ownerProfile.getAvatarUrl() : null;

        return new RoomResponse(
                room.getId(),
                room.getName(),
                room.getDescription(),
                room.getMaxMembers(),
                room.getVisibility(),
                room.getStatus(),
                room.getTopic(),
                room.getOwnerId(),
                ownerName,
                ownerAvatar,
                memberCount,
                isMember,
                room.getCreatedAt(),
                room.getUpdatedAt()
        );
    }

    private RoomMemberResponse toMemberResponse(RoomMember member, boolean isOnline, String focusStatus) {
        UserProfile profile = userProfileRepository.findById(member.getUserId()).orElse(null);
        return toMemberResponse(member, isOnline, focusStatus, profile);
    }

    private RoomMemberResponse toMemberResponse(RoomMember member, boolean isOnline,
                                                String focusStatus, UserProfile profile) {
        String displayName = profile != null ? profile.getDisplayName() : "User" + member.getUserId();
        String avatarUrl = profile != null ? profile.getAvatarUrl() : null;

        return new RoomMemberResponse(
                member.getId(),
                member.getUserId(),
                displayName,
                avatarUrl,
                member.getRole(),
                member.isMuted(),
                member.getMutedUntil(),
                member.getJoinedAt(),
                isOnline,
                focusStatus
        );
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
