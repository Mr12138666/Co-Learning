package com.colearning.room.dto.response;

import java.time.Instant;
import java.util.List;

/**
 * Reconnect snapshot for a room - used when a client reconnects after disconnection.
 */
public record RoomStateResponse(
    Long roomId,
    String roomName,
    String status,
    List<RoomMemberResponse> members,
    List<Long> onlineUserIds,
    List<RoomMessageResponse> recentMessages,
    Instant snapshotAt
) {}
