package com.eventsphere.controller;

import com.eventsphere.dto.BookingRequest;
import com.eventsphere.dto.RegistrationResponse;
import com.eventsphere.service.RegistrationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller handling seat registration for events.
 */
@RestController
@RequestMapping("/api/events")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/{eventId}/register")
    public ResponseEntity<RegistrationResponse> register(
            @PathVariable Long eventId,
            @RequestBody BookingRequest request) {
        RegistrationResponse response = registrationService.registerUserForEvent(
                request.getUserId(), eventId);
        return ResponseEntity.ok(response);
    }
}
