package com.colearning.common.config;

import com.colearning.common.security.JwtChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket/STOMP configuration for real-time room features.
 *
 * <p>Endpoints:
 * <ul>
 *   <li>{@code /ws} - STOMP endpoint with SockJS fallback</li>
 *   <li>{@code /app} - Application destination prefix for client messages</li>
 *   <li>{@code /topic} - Simple broker destination prefix for server broadcasts</li>
 * </ul>
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final JwtChannelInterceptor jwtChannelInterceptor;

    /**
     * TaskScheduler required by the simple broker for heartbeat support.
     */
    @Bean
    public ThreadPoolTaskScheduler stompHeartbeatScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("stomp-heartbeat-");
        scheduler.setDaemon(true);
        return scheduler;
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Enable simple in-memory broker for /topic destinations
        // Heartbeat: 15s server → client (keeps connections alive)
        registry.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{15000, 15000})
                .setTaskScheduler(stompHeartbeatScheduler());

        // Client messages prefixed with /app are routed to @MessageMapping methods
        registry.setApplicationDestinationPrefixes("/app");

        // User-specific destination prefix (for private messages, future use)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Register STOMP endpoint with SockJS fallback for browser compatibility
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // Add JWT interceptor to authenticate CONNECT frames
        registration.interceptors(jwtChannelInterceptor);
    }
}
