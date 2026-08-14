package com.intern.fwork.ratelimit;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe sliding window rate limiter implementation for in-memory / testing usage.
 */
public class InMemoryRateLimiter implements RateLimiter {

    private final Map<String, Deque<Long>> buckets = new ConcurrentHashMap<>();

    @Override
    public synchronized boolean tryConsume(String key, int limit, long windowMs) {
        long now = System.currentTimeMillis();
        long windowStart = now - windowMs;

        Deque<Long> timestamps = buckets.computeIfAbsent(key, k -> new ArrayDeque<>());

        // Remove expired entries
        while (!timestamps.isEmpty() && timestamps.peekFirst() <= windowStart) {
            timestamps.pollFirst();
        }

        if (timestamps.size() >= limit) {
            return false;
        }

        timestamps.addLast(now);
        return true;
    }

    public synchronized void reset() {
        buckets.clear();
    }
}
