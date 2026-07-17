package com.minimarket.security.service;

import com.minimarket.security.exception.InvalidJwtException;
import com.minimarket.security.exception.JwtFailureReason;
import com.minimarket.security.model.JwtClaims;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    public JwtClaims parseToken(String rawToken) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(rawToken)
                    .getPayload();
            return new JwtClaims(claims);
        } catch (ExpiredJwtException expiredJwtException) {
            throw new InvalidJwtException(JwtFailureReason.EXPIRED);
        } catch (MalformedJwtException malformedJwtException) {
            throw new InvalidJwtException(JwtFailureReason.MALFORMED);
        } catch (io.jsonwebtoken.security.SecurityException securityException) {
            throw new InvalidJwtException(JwtFailureReason.INVALID_SIGNATURE);
        } catch (JwtException jwtException) {
            throw new InvalidJwtException(JwtFailureReason.UNSUPPORTED);
        }
    }

    public boolean isTokenValidForUser(JwtClaims jwtClaims, UserDetails userDetails) {
        return jwtClaims.getUsername().equals(userDetails.getUsername()) && !jwtClaims.isExpired();
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toList()));
        return createToken(claims, userDetails.getUsername());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
