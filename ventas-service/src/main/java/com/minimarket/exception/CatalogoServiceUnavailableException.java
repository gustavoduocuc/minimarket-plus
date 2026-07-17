package com.minimarket.exception;

public class CatalogoServiceUnavailableException extends RuntimeException {

    public CatalogoServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public CatalogoServiceUnavailableException(String message) {
        super(message);
    }
}
