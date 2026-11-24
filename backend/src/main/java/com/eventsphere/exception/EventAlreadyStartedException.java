package com.eventsphere.exception;

/** Thrown when a booking is cancelled after its event has started. Mapped to HTTP 409 by {@link GlobalExceptionHandler}. */
public class EventAlreadyStartedException extends RuntimeException {
    public EventAlreadyStartedException(String message) {
        super(message);
    }
}
