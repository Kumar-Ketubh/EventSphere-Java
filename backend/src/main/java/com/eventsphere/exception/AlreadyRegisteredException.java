package com.eventsphere.exception;

/**
 * Thrown when a user attempts to register for an event they have already booked.
 * Mapped to HTTP 409 Conflict by {@link GlobalExceptionHandler}.
 */
public class AlreadyRegisteredException extends RuntimeException {
    public AlreadyRegisteredException(String message) {
        super(message);
    }
}
