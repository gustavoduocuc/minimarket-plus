package com.minimarket.security.service;

import com.minimarket.security.exception.AccountLockedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final Clock clock;
    private final int maxAttempts;
    private final long lockDurationMinutes;
    private final Map<String, AttemptState> attemptsByUsername = new ConcurrentHashMap<>();

    public LoginAttemptService(
            Clock clock,
            @Value("${security.login.max-attempts:5}") int maxAttempts,
            @Value("${security.login.lock-duration-minutes:15}") long lockDurationMinutes) {
        this.clock = clock;
        this.maxAttempts = maxAttempts;
        this.lockDurationMinutes = lockDurationMinutes;
    }

    public void assertNotLocked(String username) {
        AttemptState attemptState = attemptsByUsername.get(username);
        if (attemptState == null) {
            return;
        }

        if (attemptState.isLockedAt(clock.instant())) {
            throw new AccountLockedException();
        }

        if (attemptState.isLockExpired(clock.instant())) {
            attemptsByUsername.remove(username);
        }
    }

    public void recordSuccess(String username) {
        attemptsByUsername.remove(username);
    }

    public void recordFailure(String username) {
        AttemptState attemptState = attemptsByUsername.compute(username, (key, existing) -> {
            if (existing == null) {
                return AttemptState.afterFailure(1, clock.instant(), maxAttempts, lockDurationMinutes);
            }
            return existing.afterFailure(clock.instant(), maxAttempts, lockDurationMinutes);
        });

        if (attemptState.isLockedAt(clock.instant())) {
            throw new AccountLockedException();
        }
    }

    private static final class AttemptState {

        private final int failedAttempts;
        private final Instant lockedUntil;

        private AttemptState(int failedAttempts, Instant lockedUntil) {
            this.failedAttempts = failedAttempts;
            this.lockedUntil = lockedUntil;
        }

        static AttemptState afterFailure(int failedAttempts, Instant now, int maxAttempts, long lockDurationMinutes) {
            if (failedAttempts >= maxAttempts) {
                return new AttemptState(failedAttempts, now.plusSeconds(lockDurationMinutes * 60));
            }
            return new AttemptState(failedAttempts, null);
        }

        AttemptState afterFailure(Instant now, int maxAttempts, long lockDurationMinutes) {
            return afterFailure(failedAttempts + 1, now, maxAttempts, lockDurationMinutes);
        }

        boolean isLockedAt(Instant instant) {
            return lockedUntil != null && instant.isBefore(lockedUntil);
        }

        boolean isLockExpired(Instant instant) {
            return lockedUntil != null && !instant.isBefore(lockedUntil);
        }
    }
}
