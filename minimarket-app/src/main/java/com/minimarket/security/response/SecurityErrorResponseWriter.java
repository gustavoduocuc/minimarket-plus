package com.minimarket.security.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

@Component
public class SecurityErrorResponseWriter {

    private final ObjectMapper objectMapper;

    public SecurityErrorResponseWriter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void writeUnauthorized(HttpServletResponse response, String clientMessage) throws IOException {
        writeError(response, HttpStatus.UNAUTHORIZED.value(), clientMessage);
    }

    public void writeForbidden(HttpServletResponse response, String clientMessage) throws IOException {
        writeError(response, HttpStatus.FORBIDDEN.value(), clientMessage);
    }

    public void writeTooManyRequests(HttpServletResponse response, String clientMessage) throws IOException {
        writeError(response, HttpStatus.TOO_MANY_REQUESTS.value(), clientMessage);
    }

    private void writeError(HttpServletResponse response, int status, String clientMessage) throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), Map.of("error", clientMessage));
    }
}
