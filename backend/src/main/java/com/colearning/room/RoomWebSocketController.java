package com.colearning.room;

import com.colearning.common.security.PrincipalUser;
import com.colearning.room.dto.request.SendRoomMessageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket/STOMP controller for real-time room interactions.
 *
 * <p>Handles client messages sent to {@code /app/rooms/{roomId}/*}:
 * <ul>
 *   <li>{@code /join} - Register presence in a room</li>
 *   <li>{@code /heartbeat} - Refresh presence TTL (sent every 25s)</li>
 *   <li>{@code /status} - Update focus status (STUDYING/PAUSED/IDLE)</li>
 *   <li>{@code /chat} - Send a chat message (persisted + broadcast)</li>
 * </ul>
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class RoomWebSocketController {

    private final PresenceService presenceService;
    private final RoomMessageService roomMessageService;

    /**
     * Register presence when a user joins a room's WebSocket channel.
     */
    @MessageMapping("/rooms/{roomId}/join")
    public void joinRoom(@DestinationVariable Long roomId,
                         SimpMessageHeaderAccessor headerAccessor,
                         Principal principal) {
        Long userId = extractUserId(principal);
        String sessionId = headerAccessor.getSessionId();
        presenceService.connect(roomId, userId, sessionId);
        log.debug("WS join: roomId={}, userId={}, sessionId={}", roomId, userId, sessionId);
    }

    /**
     * Heartbeat to refresh presence TTL. Called by client every 25s.
     */
    @MessageMapping("/rooms/{roomId}/heartbeat")
    public void heartbeat(@DestinationVariable Long roomId,
                          Principal principal) {
        Long userId = extractUserId(principal);
        presenceService.heartbeat(roomId, userId);
    }

    /**
     * Update focus status for the current user in a room.
     */
    @MessageMapping("/rooms/{roomId}/status")
    public void updateStatus(@DestinationVariable Long roomId,
                             @Payload Map<String, String> payload,
                             Principal principal) {
        Long userId = extractUserId(principal);
        String focusStatus = payload.get("focusStatus");
        if (focusStatus == null || focusStatus.isBlank()) {
            focusStatus = "IDLE";
        }
        presenceService.updateFocusStatus(roomId, userId, focusStatus);
    }

    /**
     * Send a chat message to a room via WebSocket.
     * The message is persisted and broadcast to all subscribers by RoomMessageServiceImpl.
     */
    @MessageMapping("/rooms/{roomId}/chat")
    public void sendChat(@DestinationVariable Long roomId,
                         @Payload Map<String, String> payload,
                         Principal principal) {
        Long userId = extractUserId(principal);
        String content = payload.get("content");
        String messageType = payload.getOrDefault("messageType", "TEXT");
        String focusStatus = payload.get("focusStatus");

        if (content == null || content.isBlank()) {
            return;
        }

        SendRoomMessageRequest request = new SendRoomMessageRequest(
                content, messageType, focusStatus);
        // Message is persisted and broadcast via SimpMessagingTemplate in RoomMessageServiceImpl
        roomMessageService.sendMessage(userId, roomId, request);
    }

    private Long extractUserId(Principal principal) {
        if (principal instanceof org.springframework.security.core.Authentication auth
                && auth.getPrincipal() instanceof PrincipalUser pu) {
            return pu.userId();
        }
        throw new IllegalArgumentException("Unauthenticated WebSocket connection");
    }
}
