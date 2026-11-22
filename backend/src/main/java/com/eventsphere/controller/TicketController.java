package com.eventsphere.controller;

import com.eventsphere.dto.TicketResponse;
import com.eventsphere.service.RegistrationService;
import com.eventsphere.util.IcsBuilder;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * REST Controller for a student's ticket. The owner is read from the login token.
 */
@RestController
@RequestMapping("/api/registrations")
public class TicketController {

    private final RegistrationService registrationService;

    public TicketController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicket(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(registrationService.getTicket(Long.valueOf(jwt.getSubject()), id));
    }

    @GetMapping("/{id}/calendar.ics")
    public ResponseEntity<byte[]> getCalendar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        TicketResponse ticket = registrationService.getTicket(Long.valueOf(jwt.getSubject()), id);
        byte[] body = IcsBuilder.build(ticket, Instant.now()).getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, "text/calendar;charset=UTF-8")
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(ticket.bookingCode() + ".ics").build().toString())
                .body(body);
    }
}
