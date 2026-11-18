package com.eventsphere.service.impl;

import com.eventsphere.dto.RegistrationResponse;
import com.eventsphere.entity.Event;
import com.eventsphere.entity.Registration;
import com.eventsphere.entity.User;
import com.eventsphere.exception.AlreadyRegisteredException;
import com.eventsphere.exception.EventNotFoundException;
import com.eventsphere.exception.NoSeatsAvailableException;
import com.eventsphere.exception.UserNotFoundException;
import com.eventsphere.repository.EventRepository;
import com.eventsphere.repository.RegistrationRepository;
import com.eventsphere.repository.UserRepository;
import com.eventsphere.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Implementation of {@link RegistrationService} with pessimistic locking seat booking.
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final RegistrationRepository registrationRepository;

    public RegistrationServiceImpl(UserRepository userRepository,
                                   EventRepository eventRepository,
                                   RegistrationRepository registrationRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    @Transactional
    public RegistrationResponse registerUserForEvent(Long userId, Long eventId) {

        // STEP 1: Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        // STEP 2: Acquire pessimistic write lock on the Event row
        Event event = eventRepository.findByIdWithLock(eventId)
                .orElseThrow(() -> new EventNotFoundException("Event not found with ID: " + eventId));

        // STEP 3: Check for duplicate registration
        if (registrationRepository.existsByUserIdAndEventId(userId, eventId)) {
            throw new AlreadyRegisteredException(
                    "You are already registered for: " + event.getTitle());
        }

        // STEP 4: Check seat availability
        if (event.getBookedSeats() >= event.getTotalSeats()) {
            throw new NoSeatsAvailableException(
                    "No seats available for: " + event.getTitle());
        }

        // STEP 5: Atomically increment bookedSeats and persist
        event.setBookedSeats(event.getBookedSeats() + 1);
        eventRepository.save(event);

        // STEP 6: Create and persist Registration record
        Registration registration = Registration.builder()
                .user(user)
                .event(event)
                .registeredAt(LocalDateTime.now())
                .build();
        Registration saved = registrationRepository.save(registration);

        return RegistrationResponse.builder()
                .registrationId(saved.getId())
                .eventId(event.getId())
                .eventTitle(event.getTitle())
                .eventVenue(event.getVenue())
                .eventStartTime(event.getStartTime())
                .eventEndTime(event.getEndTime())
                .registeredAt(saved.getRegisteredAt())
                .build();
    }
}
