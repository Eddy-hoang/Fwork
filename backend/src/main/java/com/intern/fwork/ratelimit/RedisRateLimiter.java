package com.intern.fwork.ratelimit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRateLimiter implements RateLimiter {

    private final StringRedisTemplate redisTemplate;
    private final InMemoryRateLimiter inMemoryFallback = new InMemoryRateLimiter();

    /**
     * Circuit-breaker: timestamp (ms) of the last Redis failure.
     * 0 = Redis is considered healthy.
     * After RECOVERY_INTERVAL_MS has elapsed, we probe Redis again.
     */
    private final AtomicLong lastFailureTime = new AtomicLong(0L);
    private static final long RECOVERY_INTERVAL_MS = 30_000L; // retry Redis after 30s

    /**
     * Atomic Lua script implementing Sliding-Window rate limiting.
     * Runs as a single atomic operation on Redis — no race condition between
     * the ZCARD count check and the ZADD insertion.
     *
     * KEYS[1]  = rate_limit key
     * ARGV[1]  = current timestamp (ms)
     * ARGV[2]  = window start timestamp (ms)  = now - windowMs
     * ARGV[3]  = limit (max requests in window)
     * ARGV[4]  = unique member (request ID)
     * ARGV[5]  = key TTL in ms (windowMs * 2)
     *
     * Returns 1 if the request is allowed, 0 if rate-limited.
     */
    private static final DefaultRedisScript<Long> SLIDING_WINDOW_SCRIPT;

    static {
        SLIDING_WINDOW_SCRIPT = new DefaultRedisScript<>();
        SLIDING_WINDOW_SCRIPT.setResultType(Long.class);
        SLIDING_WINDOW_SCRIPT.setScriptText(
            "redis.call('ZREMRANGEBYSCORE', KEYS[1], '-inf', ARGV[2]) " +
            "local count = redis.call('ZCARD', KEYS[1]) " +
            "if count >= tonumber(ARGV[3]) then return 0 end " +
            "redis.call('ZADD', KEYS[1], ARGV[1], ARGV[4]) " +
            "redis.call('PEXPIRE', KEYS[1], ARGV[5]) " +
            "return 1"
        );
    }

    private boolean isRedisAvailable() {
        long failedAt = lastFailureTime.get();
        if (failedAt == 0L) return true;
        // Allow probing again after RECOVERY_INTERVAL_MS
        return System.currentTimeMillis() - failedAt >= RECOVERY_INTERVAL_MS;
    }

    @Override
    public boolean tryConsume(String key, int limit, long windowMs) {
        if (redisTemplate == null || !isRedisAvailable()) {
            return inMemoryFallback.tryConsume(key, limit, windowMs);
        }

        try {
            long now = System.currentTimeMillis();
            long windowStart = now - windowMs;
            String redisKey = "rate_limit:" + key;

            Long result = redisTemplate.execute(
                    SLIDING_WINDOW_SCRIPT,
                    Collections.singletonList(redisKey),
                    String.valueOf(now),
                    String.valueOf(windowStart),
                    String.valueOf(limit),
                    java.util.UUID.randomUUID().toString(),
                    String.valueOf(windowMs * 2)
            );

            // On success, reset the circuit breaker
            if (lastFailureTime.get() != 0L) {
                log.info("Redis rate limiter recovered successfully.");
                lastFailureTime.set(0L);
            }

            return result != null && result == 1L;
        } catch (Exception ex) {
            log.warn("Redis rate limiter unavailable, switching to in-memory fallback: {}", ex.getMessage());
            lastFailureTime.set(System.currentTimeMillis());
            return inMemoryFallback.tryConsume(key, limit, windowMs);
        }
    }
}

