package com.minimarket.exception;

public class ForbiddenOperationException extends RuntimeException {

    private final String clientMessage;

    public ForbiddenOperationException(String clientMessage) {
        super(clientMessage);
        this.clientMessage = clientMessage;
    }

    public String getClientMessage() {
        return clientMessage;
    }
}
