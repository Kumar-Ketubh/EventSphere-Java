package com.eventsphere.service;

import com.eventsphere.dto.RegistrationResponse;
import com.eventsphere.dto.TicketResponse;

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

    /**
     * Returns the ticket for a booking, only if it belongs to the given user.
     *
     * @throws com.eventsphere.exception.RegistrationNotFoundException  if no such booking
     * @throws com.eventsphere.exception.RegistrationForbiddenException if it is someone else's
     */
    TicketResponse getTicket(Long userId, Long registrationId);
}
