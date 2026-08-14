package com.intern.fwork.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final InMemoryRateLimiter inMemoryFallback = new InMemoryRateLimiter();
    private boolean redisAvailable = true;

    @Override
    public boolean tryConsume(String key, int limit, long windowMs) {
        if (redisTemplate == null || !redisAvailable) {
            return inMemoryFallback.tryConsume(key, limit, windowMs);
        }

        try {
            long now = System.currentTimeMillis();
            long windowStart = now - windowMs;
            String redisKey = "rate_limit:" + key;

            // Remove old entries outside the current window
            redisTemplate.opsForZSet().removeRangeByScore(redisKey, 0, windowStart);

            // Count current requests in window
            Long count = redisTemplate.opsForZSet().zCard(redisKey);

            if (count != null && count >= limit) {
                return false;
            }

            // Add current request
            redisTemplate.opsForZSet().add(redisKey, UUID.randomUUID().toString(), now);
            // Set expiration to clean up unused keys
            redisTemplate.expire(redisKey, java.time.Duration.ofMillis(windowMs * 2));

            return true;
        } catch (Exception ex) {
            log.debug("Redis rate limiter unavailable, switching to in-memory fallback: {}", ex.getMessage());
            redisAvailable = false;
            return inMemoryFallback.tryConsume(key, limit, windowMs);
        }
    }
}
