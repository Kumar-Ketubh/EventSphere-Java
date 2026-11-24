package com.eventsphere.exception;

/** Thrown when a booking belongs to someone else. Mapped to HTTP 403 by {@link GlobalExceptionHandler}. */
public class RegistrationForbiddenException extends RuntimeException {
    public RegistrationForbiddenException(String message) {
        super(message);
    }
}
