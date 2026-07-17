package com.minimarket.security.audit;

import com.minimarket.security.response.SecurityErrorResponseWriter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SecurityAuditHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private static final Logger log = LoggerFactory.getLogger(SecurityAuditHandler.class);

    private final SecurityErrorResponseWriter securityErrorResponseWriter;

    public SecurityAuditHandler(SecurityErrorResponseWriter securityErrorResponseWriter) {
        this.securityErrorResponseWriter = securityErrorResponseWriter;
    }

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         org.springframework.security.core.AuthenticationException authException)
            throws IOException {
        log.warn("Intento de acceso no autenticado - IP: {}, URI: {}",
                request.getRemoteAddr(), request.getRequestURI());
        log.debug("Detalle acceso no autenticado", authException);
        securityErrorResponseWriter.writeUnauthorized(response, "No autenticado");
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       org.springframework.security.access.AccessDeniedException accessDeniedException)
            throws IOException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth != null ? auth.getName() : "anonimo";
        log.warn("Acceso denegado - usuario: {}, IP: {}, URI: {}",
                username, request.getRemoteAddr(), request.getRequestURI());
        log.debug("Detalle acceso denegado", accessDeniedException);
        securityErrorResponseWriter.writeForbidden(response, "Acceso denegado");
    }
}
