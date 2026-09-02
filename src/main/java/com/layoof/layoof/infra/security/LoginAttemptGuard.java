package com.layoof.layoof.infra.security;

import com.layoof.layoof.exception.TooManyAttemptsException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class LoginAttemptGuard {

    static final int MAX_FAILURES = 5;
    static final Duration LOCKOUT = Duration.ofMinutes(15);

    private static final int MAX_TRACKED_ACCOUNTS = 50_000;

    private final Map<String, Attempts> byAccount = new ConcurrentHashMap<>();

    public void assertNotLocked(String account) {
        Attempts attempts = byAccount.get(account);
        if (attempts == null) {
            return;
        }
        Instant now = Instant.now();
        if (attempts.isLocked(now)) {
            throw new TooManyAttemptsException(
                    "Muitas tentativas de acesso seguidas. Tente novamente daqui a %d minutos"
                            .formatted(attempts.minutesUntilRelease(now)));
        }
    }

    public void recordFailure(String account) {
        if (byAccount.size() >= MAX_TRACKED_ACCOUNTS) {
            evictReleased();
        }
        byAccount.compute(account, (ignored, existing) -> {
            Instant now = Instant.now();
            if (existing == null || existing.isReleased(now)) {
                return new Attempts(1, now.plus(LOCKOUT));
            }
            return new Attempts(existing.failures() + 1, existing.lockedUntil());
        });
    }

    public void recordSuccess(String account) {
        byAccount.remove(account);
    }

    @Scheduled(fixedDelay = 300_000)
    void evictReleased() {
        Instant now = Instant.now();
        byAccount.values().removeIf(attempts -> attempts.isReleased(now));
    }

    private record Attempts(int failures, Instant lockedUntil) {

        private boolean isReleased(Instant now) {
            return now.isAfter(lockedUntil);
        }

        private boolean isLocked(Instant now) {
            return failures >= MAX_FAILURES && !isReleased(now);
        }

        private long minutesUntilRelease(Instant now) {
            return Math.max(1, Duration.between(now, lockedUntil).toMinutes());
        }
    }
}
