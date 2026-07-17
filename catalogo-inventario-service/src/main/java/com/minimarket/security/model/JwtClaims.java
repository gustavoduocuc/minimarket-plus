package com.minimarket.security.model;

import io.jsonwebtoken.Claims;

import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    public List<String> getRoles() {
        Object roles = claims.get("roles");
        if (!(roles instanceof List<?> roleList)) {
            return Collections.emptyList();
        }
        return roleList.stream()
                .map(Object::toString)
                .toList();
    }

    public Claims getUnderlyingClaims() {
        return claims;
    }
}
