package com.colearning.room;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Manages real-time user presence in rooms using Redis.
 *
 * <p>Presence is tracked with a Redis Set per room: {@code room:presence:{roomId}}.
 * Each set has a 35s TTL (slightly larger than the 25s client heartbeat).
 * Focus status is tracked per user per room: {@code room:focus:{roomId}:{userId}}.
 */
public interface PresenceService {

    /**
     * Records a user as online in a room and broadcasts a presence update.
     *
     * @param roomId      the room ID
     * @param userId      the user ID
     * @param sessionId   the WebSocket session ID (for disconnect cleanup)
     */
    void connect(Long roomId, Long userId, String sessionId);

    /**
     * Refreshes the TTL for a user's presence in a room (called on heartbeat).
     *
     * @param roomId the room ID
     * @param userId the user ID
     */
    void heartbeat(Long roomId, Long userId);

    /**
     * Removes a user from a room's presence and broadcasts a presence update.
     *
     * @param roomId    the room ID
     * @param userId    the user ID
     */
    void disconnect(Long roomId, Long userId);

    /**
     * Removes a user from all rooms they were present in (used on WS disconnect).
     *
     * @param sessionId the WebSocket session ID
     */
    void disconnectBySession(String sessionId);

    /**
     * Returns the set of online user IDs in a room.
     *
     * @param roomId the room ID
     * @return set of user IDs currently online
     */
    Set<Long> getOnlineUserIds(Long roomId);

    /**
     * Checks if a user is currently online in a room.
     *
     * @param roomId the room ID
     * @param userId the user ID
     * @return true if the user is online
     */
    boolean isOnline(Long roomId, Long userId);

    /**
     * Updates a user's focus status in a room and broadcasts the update.
     *
     * @param roomId      the room ID
     * @param userId      the user ID
     * @param focusStatus STUDYING | PAUSED | IDLE
     */
    void updateFocusStatus(Long roomId, Long userId, String focusStatus);

    /**
     * Gets focus status for all online users in a room.
     *
     * @param roomId the room ID
     * @return map of userId → focusStatus
     */
    Map<Long, String> getFocusStatuses(Long roomId);

    /**
     * Gets focus status for a specific user in a room.
     *
     * @param roomId the room ID
     * @param userId the user ID
     * @return focus status string, or null if not set
     */
    String getFocusStatus(Long roomId, Long userId);
}
