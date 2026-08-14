package com.intern.fwork.ratelimit;

/**
 * Strategy interface for rate limiting.
 * Allows swapping between Redis (production) and in-memory (test).
 */
public interface RateLimiter {

    /**
     * Try to consume one token from the given bucket.
     *
     * @param key       Unique key for this bucket (e.g. "ip:192.168.1.1" or "user:abc123")
     * @param limit     Max requests allowed in the window
     * @param windowMs  Window size in milliseconds
     * @return true if request is allowed, false if rate limited
     */
    boolean tryConsume(String key, int limit, long windowMs);
}
