package com.colearning.room.internal;

import com.colearning.room.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis-backed implementation of {@link PresenceService}.
 *
 * <p>Presence tracking uses a Redis Hash per room:
 * {@code room:presence:{roomId}} → field=userId, value=lastHeartbeatEpochMs.
 * Users with heartbeat older than {@link #PRESENCE_TTL_SECONDS} are considered offline.
 *
 * <p>Focus status uses individual keys: {@code room:focus:{roomId}:{userId}} → status string.
 *
 * <p>Session → (roomId, userId) mapping is kept in-memory (per-instance for single deployment).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PresenceServiceImpl implements PresenceService {

    private static final String PRESENCE_KEY_PREFIX = "room:presence:";
    private static final String FOCUS_KEY_PREFIX = "room:focus:";
    private static final long PRESENCE_TTL_SECONDS = 35; // slightly > 25s client heartbeat

    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    // In-memory session → (roomId, userId) mapping for disconnect cleanup
    private final ConcurrentHashMap<String, long[]> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void connect(Long roomId, Long userId, String sessionId) {
        String presenceKey = getPresenceKey(roomId);
        String userIdField = userId.toString();

        // Record presence with current timestamp
        redisTemplate.opsForHash().put(presenceKey, userIdField,
                String.valueOf(System.currentTimeMillis()));

        // Store session mapping
        sessionMap.put(sessionId, new long[]{roomId, userId});

        log.debug("Presence connect: roomId={}, userId={}, sessionId={}", roomId, userId, sessionId);

        // Broadcast join event
        broadcastPresenceUpdate(roomId, userId, "JOIN");
    }

    @Override
    public void heartbeat(Long roomId, Long userId) {
        String presenceKey = getPresenceKey(roomId);
        redisTemplate.opsForHash().put(presenceKey, userId.toString(),
                String.valueOf(System.currentTimeMillis()));
    }

    @Override
    public void disconnect(Long roomId, Long userId) {
        String presenceKey = getPresenceKey(roomId);
        redisTemplate.opsForHash().delete(presenceKey, userId.toString());

        // Clean up focus status
        redisTemplate.delete(getFocusKey(roomId, userId));

        log.debug("Presence disconnect: roomId={}, userId={}", roomId, userId);

        // Broadcast leave event
        broadcastPresenceUpdate(roomId, userId, "LEAVE");
    }

    @Override
    public void disconnectBySession(String sessionId) {
        long[] mapping = sessionMap.remove(sessionId);
        if (mapping != null) {
            Long roomId = mapping[0];
            Long userId = mapping[1];
            disconnect(roomId, userId);
            log.debug("Cleaned up session: sessionId={}, roomId={}, userId={}",
                    sessionId, roomId, userId);
        }
    }

    @Override
    public Set<Long> getOnlineUserIds(Long roomId) {
        String presenceKey = getPresenceKey(roomId);
        Map<Object, Object> entries = redisTemplate.opsForHash().entries(presenceKey);

        if (entries.isEmpty()) {
            return Set.of();
        }

        long cutoff = System.currentTimeMillis() - (PRESENCE_TTL_SECONDS * 1000);
        Set<Long> onlineUserIds = new HashSet<>();
        Set<Object> staleFields = new HashSet<>();

        for (Map.Entry<Object, Object> entry : entries.entrySet()) {
            try {
                long lastHeartbeat = Long.parseLong((String) entry.getValue());
                if (lastHeartbeat > cutoff) {
                    onlineUserIds.add(Long.parseLong((String) entry.getKey()));
                } else {
                    staleFields.add(entry.getKey());
                }
            } catch (NumberFormatException e) {
                staleFields.add(entry.getKey());
            }
        }

        // Clean up stale entries
        if (!staleFields.isEmpty()) {
            redisTemplate.opsForHash().delete(presenceKey,
                    staleFields.toArray());
        }

        return onlineUserIds;
    }

    @Override
    public boolean isOnline(Long roomId, Long userId) {
        String presenceKey = getPresenceKey(roomId);
        Object lastHeartbeat = redisTemplate.opsForHash().get(presenceKey, userId.toString());

        if (lastHeartbeat == null) {
            return false;
        }

        long cutoff = System.currentTimeMillis() - (PRESENCE_TTL_SECONDS * 1000);
        try {
            return Long.parseLong((String) lastHeartbeat) > cutoff;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void updateFocusStatus(Long roomId, Long userId, String focusStatus) {
        String focusKey = getFocusKey(roomId, userId);
        redisTemplate.opsForValue().set(focusKey, focusStatus,
                Duration.ofSeconds(PRESENCE_TTL_SECONDS + 5));

        log.debug("Focus status update: roomId={}, userId={}, status={}",
                roomId, userId, focusStatus);

        // Broadcast focus status update
        broadcastFocusUpdate(roomId, userId, focusStatus);
    }

    @Override
    public Map<Long, String> getFocusStatuses(Long roomId) {
        Set<Long> onlineUserIds = getOnlineUserIds(roomId);
        Map<Long, String> statuses = new HashMap<>();

        for (Long userId : onlineUserIds) {
            String status = getFocusStatus(roomId, userId);
            statuses.put(userId, status != null ? status : "IDLE");
        }

        return statuses;
    }

    @Override
    public String getFocusStatus(Long roomId, Long userId) {
        return redisTemplate.opsForValue().get(getFocusKey(roomId, userId));
    }

    // --- Private helpers ---

    private String getPresenceKey(Long roomId) {
        return PRESENCE_KEY_PREFIX + roomId;
    }

    private String getFocusKey(Long roomId, Long userId) {
        return FOCUS_KEY_PREFIX + roomId + ":" + userId;
    }

    private void broadcastPresenceUpdate(Long roomId, Long userId, String action) {
        String destination = "/topic/rooms/" + roomId + "/presence";
        Map<String, Object> payload = Map.of(
                "userId", userId,
                "action", action,
                "onlineUsers", getOnlineUserIds(roomId),
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend(destination, payload);
    }

    private void broadcastFocusUpdate(Long roomId, Long userId, String focusStatus) {
        String destination = "/topic/rooms/" + roomId + "/status";
        Map<String, Object> payload = Map.of(
                "userId", userId,
                "focusStatus", focusStatus,
                "timestamp", System.currentTimeMillis()
        );
        messagingTemplate.convertAndSend(destination, payload);
    }
}
