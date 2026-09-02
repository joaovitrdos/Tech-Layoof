package com.layoof.layoof.infra.security;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class RateLimiter {

    private static final int MAX_TRACKED_KEYS = 100_000;

    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public Decision check(String key, int limit, Duration window) {
        if (limit <= 0) {
            return Decision.permit();
        }
        if (windows.size() >= MAX_TRACKED_KEYS) {
            evictExpired();
        }

        Instant now = Instant.now();
        Window current = windows.compute(key, (ignored, existing) ->
                existing == null || existing.isExpired(now) ? new Window(now.plus(window)) : existing);

        if (current.hits.incrementAndGet() > limit) {
            return Decision.refuse(current.secondsUntilReset(now));
        }
        return Decision.permit();
    }

    public void reset(String key) {
        windows.remove(key);
    }

    @Scheduled(fixedDelay = 300_000)
    void evictExpired() {
        Instant now = Instant.now();
        windows.values().removeIf(window -> window.isExpired(now));
    }

    public record Decision(boolean allowed, long retryAfterSeconds) {

        static Decision permit() {
            return new Decision(true, 0);
        }

        static Decision refuse(long retryAfterSeconds) {
            return new Decision(false, retryAfterSeconds);
        }
    }

    private static final class Window {

        private final Instant resetAt;
        private final AtomicInteger hits = new AtomicInteger();

        private Window(Instant resetAt) {
            this.resetAt = resetAt;
        }

        private boolean isExpired(Instant now) {
            return now.isAfter(resetAt);
        }

        private long secondsUntilReset(Instant now) {
            return Math.max(1, Duration.between(now, resetAt).toSeconds());
        }
    }
}
