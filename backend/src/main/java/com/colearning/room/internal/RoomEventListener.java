package com.colearning.room.internal;

import com.colearning.room.PresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

/**
 * Listens for WebSocket session disconnect events and cleans up presence state.
 *
 * <p>When a client disconnects (network drop, tab close, etc.), the session ID
 * is used to look up the user's room membership and remove them from the
 * room's online presence set in Redis.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RoomEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void handleSessionDisconnect(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        log.debug("WebSocket session disconnected: {}", sessionId);
        presenceService.disconnectBySession(sessionId);
    }
}
