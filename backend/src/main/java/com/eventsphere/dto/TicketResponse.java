package com.eventsphere.dto;

import java.time.LocalDateTime;

/** A student's ticket: the booked event, who holds it, and the booking code. */
public record TicketResponse(
        Long registrationId,
        String bookingCode,
        Long eventId,
        String eventTitle,
        String eventVenue,
        LocalDateTime eventStartTime,
        LocalDateTime eventEndTime,
        String studentName,
        String rollNumber) {
}
