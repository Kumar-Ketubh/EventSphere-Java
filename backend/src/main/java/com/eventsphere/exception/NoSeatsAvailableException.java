package com.eventsphere.exception;

/**
 * Thrown when a registration attempt fails due to the event being fully booked.
 * Mapped to HTTP 400 Bad Request by {@link GlobalExceptionHandler}.
 */
public class NoSeatsAvailableException extends RuntimeException {
    public NoSeatsAvailableException(String message) {
        super(message);
    }
}
