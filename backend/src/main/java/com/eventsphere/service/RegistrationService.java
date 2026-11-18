package com.eventsphere.service;

import com.eventsphere.dto.RegistrationResponse;

/**
 * Service interface for seat registration operations.
 * Implementation: {@link com.eventsphere.service.impl.RegistrationServiceImpl}
 */
public interface RegistrationService {

    /**
     * Registers a user for an event with full safety checks and pessimistic locking.
     *
     * <p>Checks performed in order:
     * <ol>
     *   <li>Verify user exists</li>
     *   <li>Acquire pessimistic write lock on event row</li>
     *   <li>Check for duplicate registration</li>
     *   <li>Check seat availability</li>
     *   <li>Increment bookedSeats and persist Registration</li>
     * </ol>
     * </p>
     *
     * @param userId  the ID of the user requesting the seat
     * @param eventId the ID of the target event
     * @return a {@link RegistrationResponse} with the confirmed booking details
     */
    RegistrationResponse registerUserForEvent(Long userId, Long eventId);
}
