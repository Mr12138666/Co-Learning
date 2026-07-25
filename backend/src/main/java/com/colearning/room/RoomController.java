package com.colearning.room;

import com.colearning.common.dto.ApiResponse;
import com.colearning.common.dto.PageResponse;
import com.colearning.common.security.SecurityUtils;
import com.colearning.room.dto.request.CreateRoomRequest;
import com.colearning.room.dto.request.JoinRoomRequest;
import com.colearning.room.dto.request.MuteMemberRequest;
import com.colearning.room.dto.request.SendRoomMessageRequest;
import com.colearning.room.dto.request.UpdateRoomRequest;
import com.colearning.room.dto.response.RoomMemberResponse;
import com.colearning.room.dto.response.RoomMessageResponse;
import com.colearning.room.dto.response.RoomResponse;
import com.colearning.room.dto.response.RoomStateResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for room management: CRUD, membership, messages, and state.
 */
@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;
    private final RoomMessageService roomMessageService;

    // ===== Room CRUD =====

    @PostMapping
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(
            @Valid @RequestBody CreateRoomRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(roomService.createRoom(userId, request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<RoomResponse>>> listRooms(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(roomService.listPublicRooms(userId, pageable)));
    }

    @GetMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> getRoom(@PathVariable Long roomId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(roomService.getRoom(roomId, userId)));
    }

    @PutMapping("/{roomId}")
    public ResponseEntity<ApiResponse<RoomResponse>> updateRoom(
            @PathVariable Long roomId,
            @Valid @RequestBody UpdateRoomRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(roomService.updateRoom(userId, roomId, request)));
    }

    @DeleteMapping("/{roomId}")
    public ResponseEntity<ApiResponse<Void>> deleteRoom(@PathVariable Long roomId) {
        Long userId = SecurityUtils.getCurrentUserId();
        roomService.deleteRoom(userId, roomId);
        return ResponseEntity.ok(ApiResponse.message("Room deleted"));
    }

    // ===== Membership =====

    @PostMapping("/{roomId}/join")
    public ResponseEntity<ApiResponse<RoomMemberResponse>> joinRoom(
            @PathVariable Long roomId,
            @RequestBody(required = false) JoinRoomRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(roomService.joinRoom(userId, roomId, request)));
    }

    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<Void>> leaveRoom(@PathVariable Long roomId) {
        Long userId = SecurityUtils.getCurrentUserId();
        roomService.leaveRoom(userId, roomId);
        return ResponseEntity.ok(ApiResponse.message("Left the room"));
    }

    @GetMapping("/{roomId}/members")
    public ResponseEntity<ApiResponse<List<RoomMemberResponse>>> listMembers(
            @PathVariable Long roomId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(roomService.listMembers(roomId, userId)));
    }

    // ===== Moderation =====

    @PostMapping("/{roomId}/kick/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> kickMember(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId) {
        Long userId = SecurityUtils.getCurrentUserId();
        roomService.kickMember(userId, roomId, targetUserId);
        return ResponseEntity.ok(ApiResponse.message("Member kicked"));
    }

    @PostMapping("/{roomId}/mute/{targetUserId}")
    public ResponseEntity<ApiResponse<Void>> muteMember(
            @PathVariable Long roomId,
            @PathVariable Long targetUserId,
            @RequestBody(required = false) MuteMemberRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        roomService.muteMember(userId, roomId, targetUserId, request);
        return ResponseEntity.ok(ApiResponse.message("Member muted"));
    }

    // ===== Messages =====

    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<PageResponse<RoomMessageResponse>>> listMessages(
            @PathVariable Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {
        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.ok(
                roomMessageService.listMessages(roomId, userId, pageable)));
    }

    @PostMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<RoomMessageResponse>> sendMessage(
            @PathVariable Long roomId,
            @Valid @RequestBody SendRoomMessageRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(
                roomMessageService.sendMessage(userId, roomId, request)));
    }

    // ===== Room State (reconnect snapshot) =====

    @GetMapping("/{roomId}/state")
    public ResponseEntity<ApiResponse<RoomStateResponse>> getRoomState(
            @PathVariable Long roomId) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.ok(roomService.getRoomState(roomId, userId)));
    }
}
