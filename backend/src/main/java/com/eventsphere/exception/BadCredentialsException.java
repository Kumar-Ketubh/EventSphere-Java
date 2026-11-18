package com.eventsphere.exception;

/**
 * Thrown when a login attempt fails due to incorrect email or password.
 * Mapped to HTTP 401 Unauthorized by {@link GlobalExceptionHandler}.
 */
public class BadCredentialsException extends RuntimeException {
    public BadCredentialsException(String message) {
        super(message);
    }
}
