package com.eventsphere.controller;

import com.eventsphere.dto.TicketResponse;
import com.eventsphere.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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
}
