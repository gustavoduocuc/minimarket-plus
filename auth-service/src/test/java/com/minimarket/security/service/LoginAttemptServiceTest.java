package com.minimarket.security.service;

import com.minimarket.security.exception.AccountLockedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {

    private static final Instant fixedInstant = Instant.parse("2026-06-04T12:00:00Z");

    private LoginAttemptService loginAttemptService;

    @BeforeEach
    void setUp() {
        Clock fixedClock = Clock.fixed(fixedInstant, ZoneOffset.UTC);
        loginAttemptService = new LoginAttemptService(fixedClock, 3, 15);
    }

    @Test
    void locksAccountAfterMaxFailedAttempts() {
        assertDoesNotThrow(() -> loginAttemptService.recordFailure("user1"));
        assertDoesNotThrow(() -> loginAttemptService.recordFailure("user1"));

        assertThrows(AccountLockedException.class, () -> loginAttemptService.recordFailure("user1"));
    }

    @Test
    void rejectsLoginWhileAccountIsLocked() {
        loginAttemptService.recordFailure("locked-user");
        loginAttemptService.recordFailure("locked-user");
        assertThrows(AccountLockedException.class, () -> loginAttemptService.recordFailure("locked-user"));

        assertThrows(AccountLockedException.class, () -> loginAttemptService.assertNotLocked("locked-user"));
    }

    @Test
    void clearsAttemptsAfterSuccessfulLogin() {
        loginAttemptService.recordFailure("user1");
        loginAttemptService.recordFailure("user1");
        loginAttemptService.recordSuccess("user1");

        assertDoesNotThrow(() -> loginAttemptService.assertNotLocked("user1"));
    }
}
