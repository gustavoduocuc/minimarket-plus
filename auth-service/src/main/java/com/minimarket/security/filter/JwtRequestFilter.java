package com.minimarket.security.filter;

import com.minimarket.security.exception.InvalidJwtException;
import com.minimarket.security.model.JwtClaims;
import com.minimarket.security.response.SecurityErrorResponseWriter;
import com.minimarket.security.service.CustomUserDetailsService;
import com.minimarket.security.service.JwtTokenService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtRequestFilter.class);
    private static final String bearerPrefix = "Bearer ";

    private final JwtTokenService jwtTokenService;
    private final CustomUserDetailsService userDetailsService;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    public JwtRequestFilter(
            JwtTokenService jwtTokenService,
            CustomUserDetailsService userDetailsService,
            SecurityErrorResponseWriter securityErrorResponseWriter) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
        this.securityErrorResponseWriter = securityErrorResponseWriter;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authorizationHeader = request.getHeader("Authorization");

        if (authorizationHeader == null || !authorizationHeader.startsWith(bearerPrefix)) {
            filterChain.doFilter(request, response);
            return;
        }

        String jwt = authorizationHeader.substring(bearerPrefix.length());

        try {
            JwtClaims jwtClaims = jwtTokenService.parseToken(jwt);

            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                boolean authenticated = tryAuthenticateRequest(request, jwtClaims);
                if (!authenticated) {
                    securityErrorResponseWriter.writeUnauthorized(response, "Token inválido o expirado");
                    return;
                }
            }

            filterChain.doFilter(request, response);
        } catch (InvalidJwtException invalidJwtException) {
            rejectJwt(request, response, invalidJwtException);
        }
    }

    private boolean tryAuthenticateRequest(HttpServletRequest request, JwtClaims jwtClaims) {
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(jwtClaims.getUsername());

            if (!jwtTokenService.isTokenValidForUser(jwtClaims, userDetails)) {
                return false;
            }

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            return true;
        } catch (UsernameNotFoundException usernameNotFoundException) {
            return false;
        }
    }

    private void rejectJwt(HttpServletRequest request, HttpServletResponse response, InvalidJwtException exception)
            throws IOException {
        log.warn("JWT rechazado - uri={}, ip={}, reason={}",
                request.getRequestURI(), request.getRemoteAddr(), exception.getReason());
        log.debug("Detalle JWT rechazado", exception);
        securityErrorResponseWriter.writeUnauthorized(response, exception.getClientMessage());
    }
}
