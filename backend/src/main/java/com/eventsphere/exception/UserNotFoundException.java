package com.eventsphere.exception;

/**
 * Thrown when a user lookup by ID or email returns no result.
 * Mapped to HTTP 404 by {@link GlobalExceptionHandler}.
 */
public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException(String message) {
        super(message);
    }
}
