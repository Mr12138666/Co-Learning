package com.colearning.common.cache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Generic cache helper for Redis operations.
 * Provides common caching patterns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheHelper {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Set a value in cache.
     *
     * @param key   Cache key
     * @param value Cache value
     */
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }
    
    /**
     * Set a value in cache with expiration.
     *
     * @param key     Cache key
     * @param value   Cache value
     * @param timeout Timeout
     * @param unit    Time unit
     */
    public void set(String key, Object value, long timeout, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, timeout, unit);
    }
    
    /**
     * Get a value from cache.
     *
     * @param key Cache key
     * @return Cached value or null
     */
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    /**
     * Get a typed value from cache.
     *
     * @param key   Cache key
     * @param clazz Expected type
     * @param <T>   Type
     * @return Cached value or null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        Object value = redisTemplate.opsForValue().get(key);
        if (value != null && clazz.isInstance(value)) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Delete a value from cache.
     *
     * @param key Cache key
     * @return true if deleted
     */
    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }
    
    /**
     * Delete multiple values from cache.
     *
     * @param keys Cache keys
     * @return Number of deleted keys
     */
    public long delete(Collection<String> keys) {
        Long count = redisTemplate.delete(keys);
        return count != null ? count : 0;
    }
    
    /**
     * Check if a key exists.
     *
     * @param key Cache key
     * @return true if exists
     */
    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
    
    /**
     * Set expiration on a key.
     *
     * @param key     Cache key
     * @param timeout Timeout
     * @param unit    Time unit
     * @return true if set
     */
    public boolean expire(String key, long timeout, TimeUnit unit) {
        return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
    }
    
    /**
     * Get the expiration of a key.
     *
     * @param key Cache key
     * @param unit Time unit
     * @return Expiration time or null
     */
    public Long getExpire(String key, TimeUnit unit) {
        return redisTemplate.getExpire(key, unit);
    }
    
    /**
     * Increment a value.
     *
     * @param key   Cache key
     * @param delta Increment value
     * @return New value
     */
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
    
    /**
     * Decrement a value.
     *
     * @param key   Cache key
     * @param delta Decrement value
     * @return New value
     */
    public Long decrement(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, -delta);
    }
    
    /**
     * Get or set pattern: get from cache, if not present, compute and cache.
     *
     * @param key      Cache key
     * @param clazz    Expected type
     * @param supplier Supplier for computing value
     * @param timeout  Timeout
     * @param unit     Time unit
     * @param <T>      Type
     * @return Cached or computed value
     */
    public <T> T getOrSet(String key, Class<T> clazz, java.util.function.Supplier<T> supplier, 
                          long timeout, TimeUnit unit) {
        T value = get(key, clazz);
        if (value == null) {
            value = supplier.get();
            if (value != null) {
                set(key, value, timeout, unit);
            }
        }
        return value;
    }
}