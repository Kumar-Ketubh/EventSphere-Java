package com.eventsphere.controller;

import com.eventsphere.dto.DashboardResponse;
import com.eventsphere.service.EventService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller providing the event dashboard data.
 */
@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /** Same lists as the dashboard, open to everyone so the landing page can show what is on. Event data only, no user data. */
    @GetMapping("/public")
    public ResponseEntity<DashboardResponse> getPublicEvents() {
        return ResponseEntity.ok(eventService.getDashboardData());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(eventService.getDashboardData());
    }
}
