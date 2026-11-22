package com.eventsphere.exception;

import com.eventsphere.dto.MessageResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Centralized exception handler for all REST controllers.
 *
 * <p>Maps domain-specific exceptions to appropriate HTTP status codes
 * and wraps messages in {@link MessageResponse} for consistent API error format.</p>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles user-not-found errors → HTTP 404.
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<MessageResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles event-not-found errors → HTTP 404.
     */
    @ExceptionHandler(EventNotFoundException.class)
    public ResponseEntity<MessageResponse> handleEventNotFound(EventNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles unknown bookings → HTTP 404.
     */
    @ExceptionHandler(RegistrationNotFoundException.class)
    public ResponseEntity<MessageResponse> handleRegistrationNotFound(RegistrationNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles someone else's booking → HTTP 403.
     */
    @ExceptionHandler(RegistrationForbiddenException.class)
    public ResponseEntity<MessageResponse> handleRegistrationForbidden(RegistrationForbiddenException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles cancelling after the event started → HTTP 409.
     */
    @ExceptionHandler(EventAlreadyStartedException.class)
    public ResponseEntity<MessageResponse> handleEventAlreadyStarted(EventAlreadyStartedException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles duplicate registration attempts → HTTP 409 Conflict.
     */
    @ExceptionHandler(AlreadyRegisteredException.class)
    public ResponseEntity<MessageResponse> handleAlreadyRegistered(AlreadyRegisteredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles fully booked events → HTTP 400 Bad Request.
     */
    @ExceptionHandler(NoSeatsAvailableException.class)
    public ResponseEntity<MessageResponse> handleNoSeats(NoSeatsAvailableException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Handles bad credentials on login → HTTP 401 Unauthorized.
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<MessageResponse> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(new MessageResponse(ex.getMessage()));
    }

    /**
     * Fallback handler for all unhandled exceptions → HTTP 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<MessageResponse> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new MessageResponse("An unexpected error occurred: " + ex.getMessage()));
    }
}
