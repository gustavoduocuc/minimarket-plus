package com.minimarket.security.exception;

public class AccountLockedException extends RuntimeException {

    private static final String clientMessage = "Demasiados intentos. Intente más tarde";

    public AccountLockedException() {
        super("account_locked");
    }

    public String getClientMessage() {
        return clientMessage;
    }
}
