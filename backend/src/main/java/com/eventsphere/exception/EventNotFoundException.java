package com.eventsphere.exception;

/**
 * Thrown when an event lookup returns no result.
 * Mapped to HTTP 404 by {@link GlobalExceptionHandler}.
 */
public class EventNotFoundException extends RuntimeException {
    public EventNotFoundException(String message) {
        super(message);
    }
}
