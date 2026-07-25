package com.colearning.room;

import com.colearning.common.dto.PageResponse;
import com.colearning.room.dto.request.CreateRoomRequest;
import com.colearning.room.dto.request.JoinRoomRequest;
import com.colearning.room.dto.request.MuteMemberRequest;
import com.colearning.room.dto.request.UpdateRoomRequest;
import com.colearning.room.dto.response.RoomMemberResponse;
import com.colearning.room.dto.response.RoomResponse;
import com.colearning.room.dto.response.RoomStateResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for managing study companion rooms: CRUD, membership, and moderation.
 */
public interface RoomService {

    // ===== Room CRUD =====

    RoomResponse createRoom(Long userId, CreateRoomRequest request);

    PageResponse<RoomResponse> listPublicRooms(Long userId, Pageable pageable);

    RoomResponse getRoom(Long roomId, Long userId);

    RoomResponse updateRoom(Long userId, Long roomId, UpdateRoomRequest request);

    void deleteRoom(Long userId, Long roomId);

    // ===== Membership =====

    RoomMemberResponse joinRoom(Long userId, Long roomId, JoinRoomRequest request);

    void leaveRoom(Long userId, Long roomId);

    List<RoomMemberResponse> listMembers(Long roomId, Long userId);

    // ===== Moderation =====

    void kickMember(Long userId, Long roomId, Long targetUserId);

    void muteMember(Long userId, Long roomId, Long targetUserId, MuteMemberRequest request);

    // ===== Room State (reconnect snapshot) =====

    RoomStateResponse getRoomState(Long roomId, Long userId);
}
