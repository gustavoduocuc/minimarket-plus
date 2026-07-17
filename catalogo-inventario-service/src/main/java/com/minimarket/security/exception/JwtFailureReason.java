package com.minimarket.security.exception;

public enum JwtFailureReason {
    EXPIRED,
    MALFORMED,
    INVALID_SIGNATURE,
    UNSUPPORTED
}
