package com.colearning.common.ratelimit;

import com.colearning.common.exception.BusinessException;
import com.colearning.common.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * AOP aspect that enforces rate limits using Redis atomic increment.
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    @Around("@annotation(rateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String identifier = resolveIdentifier(joinPoint, rateLimit);
        String redisKey = "rate:" + rateLimit.key() + ":" + identifier;

        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(rateLimit.window()));
        }

        if (count != null && count > rateLimit.limit()) {
            log.warn("Rate limit exceeded: key={}, count={}, limit={}",
                    redisKey, count, rateLimit.limit());
            throw BusinessException.of(ErrorCode.TOO_MANY_REQUESTS);
        }

        return joinPoint.proceed();
    }

    private String resolveIdentifier(ProceedingJoinPoint joinPoint, RateLimit rateLimit) {
        // If identifier expression is empty, use client IP
        if (rateLimit.identifier().isEmpty()) {
            return getClientIp();
        }

        // For simplicity, use the first argument if it's a string, otherwise use IP
        // A full SpEL implementation would be more robust but this covers the common cases
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String[] paramNames = signature.getParameterNames();

        String expr = rateLimit.identifier();
        if (expr.startsWith("#") && paramNames != null) {
            String paramName = expr.substring(1);
            for (int i = 0; i < paramNames.length; i++) {
                if (paramNames[i].equals(paramName) && args[i] != null) {
                    return args[i].toString();
                }
            }
        }

        return getClientIp();
    }

    private String getClientIp() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String forwarded = request.getHeader("X-Forwarded-For");
                if (forwarded != null && !forwarded.isEmpty()) {
                    return forwarded.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            log.debug("Could not get client IP", e);
        }
        return "unknown";
    }
}
