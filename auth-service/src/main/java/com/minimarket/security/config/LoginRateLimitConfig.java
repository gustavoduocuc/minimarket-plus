package com.minimarket.security.config;

import com.minimarket.security.response.SecurityErrorResponseWriter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Configuration
public class LoginRateLimitConfig {

    @Bean
    public LoginRateLimitFilter loginRateLimitFilter(
            Clock clock,
            SecurityErrorResponseWriter securityErrorResponseWriter,
            @Value("${security.login.rate-limit.max-requests:20}") int maxRequests,
            @Value("${security.login.rate-limit.window-seconds:60}") long windowSeconds) {
        return new LoginRateLimitFilter(clock, securityErrorResponseWriter, maxRequests, windowSeconds);
    }
}

class LoginRateLimitFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(LoginRateLimitFilter.class);
    private static final String loginPath = "/api/auth/login";

    private final Clock clock;
    private final SecurityErrorResponseWriter securityErrorResponseWriter;
    private final int maxRequests;
    private final long windowSeconds;
    private final Map<String, ClientRequestTimestamps> requestsByClientKey = new ConcurrentHashMap<>();

    LoginRateLimitFilter(
            Clock clock,
            SecurityErrorResponseWriter securityErrorResponseWriter,
            int maxRequests,
            long windowSeconds) {
        this.clock = clock;
        this.securityErrorResponseWriter = securityErrorResponseWriter;
        this.maxRequests = maxRequests;
        this.windowSeconds = windowSeconds;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        if (!isLoginRequest(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = resolveClientKey(request);

        if (isRateLimitExceeded(clientKey)) {
            log.warn("Rate limit de login excedido - ip={}, uri={}", clientKey, request.getRequestURI());
            securityErrorResponseWriter.writeTooManyRequests(response, "Demasiadas solicitudes. Intente más tarde");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean isLoginRequest(HttpServletRequest request) {
        return HttpMethod.POST.name().equals(request.getMethod()) && loginPath.equals(request.getRequestURI());
    }

    private boolean isRateLimitExceeded(String clientKey) {
        Instant now = clock.instant();
        Instant windowStart = now.minusSeconds(windowSeconds);

        ClientRequestTimestamps requestTimestamps = requestsByClientKey.computeIfAbsent(
                clientKey, ignored -> new ClientRequestTimestamps());
        requestTimestamps.removeOlderThan(windowStart);
        requestTimestamps.add(now);

        return requestTimestamps.count() > maxRequests;
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}

class ClientRequestTimestamps {

    private final Deque<Instant> timestamps = new ArrayDeque<>();

    void removeOlderThan(Instant windowStart) {
        while (!timestamps.isEmpty() && timestamps.peekFirst().isBefore(windowStart)) {
            timestamps.removeFirst();
        }
    }

    void add(Instant timestamp) {
        timestamps.addLast(timestamp);
    }

    int count() {
        return timestamps.size();
    }
}
