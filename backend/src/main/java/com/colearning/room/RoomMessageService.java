package com.colearning.room;

import com.colearning.common.dto.PageResponse;
import com.colearning.room.dto.request.SendRoomMessageRequest;
import com.colearning.room.dto.response.RoomMessageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Service for managing room chat messages.
 */
public interface RoomMessageService {

    /**
     * Sends a message to a room (persists and broadcasts via WebSocket).
     */
    RoomMessageResponse sendMessage(Long userId, Long roomId, SendRoomMessageRequest request);

    /**
     * Lists messages in a room with pagination (newest first).
     */
    PageResponse<RoomMessageResponse> listMessages(Long roomId, Long userId, Pageable pageable);

    /**
     * Gets recent messages for reconnect snapshot (oldest to newest).
     */
    List<RoomMessageResponse> getRecentMessages(Long roomId, int limit);
}
