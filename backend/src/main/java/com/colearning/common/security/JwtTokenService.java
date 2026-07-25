package com.colearning.common.security;

import com.colearning.common.config.AppProperties;
import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * JWT token service: generates and validates Access Tokens (in-memory, short-lived)
 * and Refresh Tokens (stored in Redis, long-lived with rotation support).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwtTokenService {

    private final AppProperties appProperties;
    private final RedisTemplate<String, Object> redisTemplate;

    private SecretKey signingKey;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @PostConstruct
    public void init() {
        byte[] keyBytes = appProperties.getJwt().getSecret().getBytes();
        // Ensure key is at least 256 bits (32 bytes) for HS256
        if (keyBytes.length < 32) {
            byte[] padded = new byte[32];
            System.arraycopy(keyBytes, 0, padded, 0, keyBytes.length);
            keyBytes = padded;
        }
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    // ===== Access Token =====

    public String generateAccessToken(PrincipalUser principal) {
        Instant now = Instant.now();
        Instant expiry = now.plusSeconds(appProperties.getJwt().getAccessTokenTtl());

        return Jwts.builder()
                .subject(principal.userId().toString())
                .claim("email", principal.email())
                .claim("role", principal.role())
                .claim("emailVerified", principal.emailVerified())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public PrincipalUser parseAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.get("email", String.class);
            String role = claims.get("role", String.class);
            Boolean emailVerified = claims.get("emailVerified", Boolean.class);

            return new PrincipalUser(userId, email, role, Boolean.TRUE.equals(emailVerified));
        } catch (JwtException e) {
            log.debug("Invalid JWT token: {}", e.getMessage());
            throw BusinessException.of(ErrorCode.AUTH_TOKEN_INVALID);
        }
    }

    // ===== Refresh Token =====

    /**
     * Generates a refresh token and stores its payload in Redis.
     * @return the refresh token UUID (to be set as HttpOnly cookie)
     */
    public String generateRefreshToken(PrincipalUser principal) {
        String tokenId = UUID.randomUUID().toString();
        String redisKey = getRefreshTokenKey(tokenId);

        Map<String, Object> payload = Map.of(
                "userId", principal.userId(),
                "email", principal.email(),
                "role", principal.role(),
                "emailVerified", principal.emailVerified(),
                "createdAt", Instant.now().toString()
        );

        redisTemplate.opsForValue().set(redisKey, payload,
                java.time.Duration.ofSeconds(appProperties.getJwt().getRefreshTokenTtl()));

        return tokenId;
    }

    /**
     * Validates a refresh token and returns the associated principal.
     * Throws if token is invalid, expired, or revoked.
     */
    @SuppressWarnings("unchecked")
    public PrincipalUser validateRefreshToken(String tokenId) {
        String redisKey = getRefreshTokenKey(tokenId);
        Object raw = redisTemplate.opsForValue().get(redisKey);

        if (raw == null) {
            throw BusinessException.of(ErrorCode.AUTH_REFRESH_TOKEN_INVALID);
        }

        Map<String, Object> payload = convertToMap(raw);
        return new PrincipalUser(
                ((Number) payload.get("userId")).longValue(),
                (String) payload.get("email"),
                (String) payload.get("role"),
                Boolean.TRUE.equals(payload.get("emailVerified"))
        );
    }

    /**
     * Rotates a refresh token: revokes the old one and issues a new one.
     */
    public String rotateRefreshToken(String oldTokenId, PrincipalUser principal) {
        revokeRefreshToken(oldTokenId);
        return generateRefreshToken(principal);
    }

    /**
     * Revokes a refresh token by deleting it from Redis.
     */
    public void revokeRefreshToken(String tokenId) {
        if (tokenId != null && !tokenId.isBlank()) {
            redisTemplate.delete(getRefreshTokenKey(tokenId));
        }
    }

    private String getRefreshTokenKey(String tokenId) {
        return appProperties.getJwt().getRefreshTokenPrefix() + tokenId;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> convertToMap(Object raw) {
        if (raw instanceof Map) {
            return (Map<String, Object>) raw;
        }
        return objectMapper.convertValue(raw, Map.class);
    }
}
