package com.eventsphere.exception;

/** Thrown when a booking does not exist. Mapped to HTTP 404 by {@link GlobalExceptionHandler}. */
public class RegistrationNotFoundException extends RuntimeException {
    public RegistrationNotFoundException(String message) {
        super(message);
    }
}
