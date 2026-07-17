package com.minimarket.security.model;

import io.jsonwebtoken.Claims;

import java.util.Date;

public class JwtClaims {

    private final Claims claims;

    public JwtClaims(Claims claims) {
        this.claims = claims;
    }

    public String getUsername() {
        return claims.getSubject();
    }

    public boolean isExpired() {
        Date expiration = claims.getExpiration();
        return expiration != null && expiration.before(new Date());
    }

    public Claims getUnderlyingClaims() {
        return claims;
    }
}
