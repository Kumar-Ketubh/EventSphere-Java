package com.eventsphere.dto;

/**
 * DTO for the seat registration request body.
 * Maps to POST /api/events/{eventId}/register request payload.
 */
public class BookingRequest {
    private Long userId;

    public BookingRequest() {
    }

    public BookingRequest(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
