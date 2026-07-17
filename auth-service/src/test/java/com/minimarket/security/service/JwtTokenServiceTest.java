package com.minimarket.security.service;

import com.minimarket.security.exception.InvalidJwtException;
import com.minimarket.security.exception.JwtFailureReason;
import com.minimarket.security.model.JwtClaims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.lang.NonNull;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenServiceTest {

    private static final String secret = "MiniMarketPlusSecretKey2026SuperSeguraYFuerte";
    private static final long expirationMs = 3_600_000L;

    @NonNull
    private final JwtTokenService jwtTokenService = new JwtTokenService();

    @BeforeEach
    void setUp() {
        configureJwtTokenService(jwtTokenService, expirationMs);
    }

    private void configureJwtTokenService(@NonNull JwtTokenService service, long expiration) {
        ReflectionTestUtils.setField(Objects.requireNonNull(service), "secret", secret);
        ReflectionTestUtils.setField(Objects.requireNonNull(service), "expiration", expiration);
    }

    @Test
    void parsesValidTokenAndValidatesUser() {
        UserDetails userDetails = new User("admin", "pass", java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        String token = jwtTokenService.generateToken(userDetails);

        JwtClaims claims = jwtTokenService.parseToken(token);

        assertEquals("admin", claims.getUsername());
        assertFalse(claims.isExpired());
        assertTrue(jwtTokenService.isTokenValidForUser(claims, userDetails));
    }

    @Test
    void rejectsMalformedToken() {
        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> jwtTokenService.parseToken("not.a.jwt"));

        assertEquals(JwtFailureReason.MALFORMED, exception.getReason());
    }

    @Test
    void rejectsTokenWithInvalidSignature() {
        UserDetails userDetails = new User("admin", "pass", java.util.List.of());
        String token = jwtTokenService.generateToken(userDetails);
        String tamperedToken = token.substring(0, token.length() - 2) + "xx";

        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> jwtTokenService.parseToken(tamperedToken));

        assertEquals(JwtFailureReason.INVALID_SIGNATURE, exception.getReason());
    }

    @Test
    void rejectsTokenForDifferentUser() {
        UserDetails admin = new User("admin", "pass", java.util.List.of());
        UserDetails other = new User("other", "pass", java.util.List.of());
        String token = jwtTokenService.generateToken(admin);

        JwtClaims claims = jwtTokenService.parseToken(token);

        assertFalse(jwtTokenService.isTokenValidForUser(claims, other));
    }

    @Test
    void rejectsExpiredToken() {
        JwtTokenService serviceWithExpiredTokens = new JwtTokenService();
        configureJwtTokenService(serviceWithExpiredTokens, -1000L);

        UserDetails userDetails = new User("admin", "pass", List.of());
        String expiredToken = serviceWithExpiredTokens.generateToken(userDetails);

        InvalidJwtException exception = assertThrows(InvalidJwtException.class,
                () -> serviceWithExpiredTokens.parseToken(expiredToken));

        assertEquals(JwtFailureReason.EXPIRED, exception.getReason());
    }
}
