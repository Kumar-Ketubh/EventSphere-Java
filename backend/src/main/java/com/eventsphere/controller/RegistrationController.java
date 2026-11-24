package com.eventsphere.controller;

import com.eventsphere.dto.RegistrationResponse;
import com.eventsphere.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for booking a seat. The booking is made for the user in the login token.
 */
@RestController
@RequestMapping("/api/events")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/{eventId}/registrations")
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable Long eventId,
            @AuthenticationPrincipal Jwt jwt) {
        RegistrationResponse response = registrationService.registerUserForEvent(
                Long.valueOf(jwt.getSubject()), eventId);
        return ResponseEntity.ok(response);
    }
}
