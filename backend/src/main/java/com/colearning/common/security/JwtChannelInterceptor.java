package com.colearning.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Intercepts STOMP CONNECT frames to authenticate WebSocket connections via JWT.
 *
 * <p>The client must include an {@code Authorization} header with a Bearer token
 * in the STOMP CONNECT frame. This interceptor extracts and validates the token,
 * then sets the authenticated {@link PrincipalUser} as the message's user principal.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtTokenService jwtTokenService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(
                message, StompHeaderAccessor.class);

        if (accessor == null) {
            return message;
        }

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            String authHeader = accessor.getFirstNativeHeader("Authorization");

            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                log.warn("WebSocket CONNECT without valid Authorization header");
                throw new IllegalArgumentException("Missing or invalid Authorization header");
            }

            String token = authHeader.substring(7);
            try {
                PrincipalUser principal = jwtTokenService.parseAccessToken(token);

                // Set the authenticated user as the STOMP session's principal
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + principal.role()));
                var authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, authorities);
                accessor.setUser(authentication);

                log.debug("WebSocket CONNECT authenticated: userId={}, email={}",
                        principal.userId(), principal.email());
            } catch (Exception e) {
                log.warn("WebSocket CONNECT authentication failed: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid or expired token");
            }
        }

        return message;
    }
}
