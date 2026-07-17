package com.minimarket.security.exception;

public class InvalidJwtException extends RuntimeException {

    private static final String clientMessage = "Token inválido o expirado";

    private final JwtFailureReason reason;

    public InvalidJwtException(JwtFailureReason reason) {
        super(reason.name());
        this.reason = reason;
    }

    public JwtFailureReason getReason() {
        return reason;
    }

    public String getClientMessage() {
        return clientMessage;
    }
}
