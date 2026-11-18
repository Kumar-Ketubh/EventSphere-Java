package com.eventsphere.service;

import com.eventsphere.dto.DashboardResponse;

/**
 * Service interface for event-related operations.
 * Implementation: {@link com.eventsphere.service.impl.EventServiceImpl}
 */
public interface EventService {

    /**
     * Computes the dashboard data by categorizing all events against
     * the current system time ({@code LocalDateTime.now()}).
     *
     * @return a {@link DashboardResponse} containing live, upcoming, and previous event lists
     */
    DashboardResponse getDashboardData();
}
