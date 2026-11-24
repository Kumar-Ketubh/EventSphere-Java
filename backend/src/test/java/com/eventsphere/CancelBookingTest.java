package com.eventsphere;

import com.eventsphere.controller.TicketController;
import com.eventsphere.entity.Event;
import com.eventsphere.entity.Registration;
import com.eventsphere.entity.User;
import com.eventsphere.exception.EventAlreadyStartedException;
import com.eventsphere.exception.GlobalExceptionHandler;
import com.eventsphere.exception.RegistrationNotFoundException;
import com.eventsphere.exception.RegistrationForbiddenException;
import com.eventsphere.repository.EventRepository;
import com.eventsphere.repository.RegistrationRepository;
import com.eventsphere.repository.UserRepository;
import com.eventsphere.service.impl.RegistrationServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Cancelling a booking: owner before start frees the seat; after start or someone else's is refused. */
class CancelBookingTest {

    private final EventRepository events = mock(EventRepository.class);
    private final RegistrationRepository registrations = mock(RegistrationRepository.class);
    private final TicketController controller = new TicketController(
            new RegistrationServiceImpl(mock(UserRepository.class), events, registrations));
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static Jwt tokenFor(long userId) {
        return new Jwt("t", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "HS256"), Map.of("sub", String.valueOf(userId)));
    }

    private Event bookingFor(long userId, long registrationId, LocalDateTime start) {
        User user = User.builder().id(userId).name("Asha Rao").build();
        Event event = Event.builder().id(5L).title("Hack Night").venue("Hall A")
                .startTime(start).endTime(start.plusHours(3)).totalSeats(10).bookedSeats(4).build();
        Registration reg = Registration.builder().id(registrationId).user(user).event(event)
                .registeredAt(LocalDateTime.now()).build();
        when(registrations.findWithDetailsById(registrationId)).thenReturn(Optional.of(reg));
        when(events.findByIdWithLock(5L)).thenReturn(Optional.of(event));
        return event;
    }

    @Test
    void ownerCancelsBeforeStartAndSeatIsFreed() {
        Event event = bookingFor(1L, 42L, LocalDateTime.now().plusDays(2));
        assertEquals(HttpStatus.NO_CONTENT, controller.cancel(42L, tokenFor(1L)).getStatusCode());
        assertEquals(3, event.getBookedSeats());
        verify(registrations).delete(org.mockito.ArgumentMatchers.any(Registration.class));
        verify(events).findByIdWithLock(5L);
    }

    @Test
    void cancelAfterStartIsRefused() {
        Event event = bookingFor(1L, 42L, LocalDateTime.now().minusHours(1));
        EventAlreadyStartedException ex = assertThrows(EventAlreadyStartedException.class,
                () -> controller.cancel(42L, tokenFor(1L)));
        assertEquals(HttpStatus.CONFLICT, handler.handleEventAlreadyStarted(ex).getStatusCode());
        assertEquals(4, event.getBookedSeats());
        verify(registrations, never()).delete(org.mockito.ArgumentMatchers.any(Registration.class));
    }

    @Test
    void otherStudentsBookingIsForbidden() {
        Event event = bookingFor(1L, 42L, LocalDateTime.now().plusDays(2));
        RegistrationForbiddenException ex = assertThrows(RegistrationForbiddenException.class,
                () -> controller.cancel(42L, tokenFor(2L)));
        assertEquals(HttpStatus.FORBIDDEN, handler.handleRegistrationForbidden(ex).getStatusCode());
        assertEquals(4, event.getBookedSeats());
        verify(registrations, never()).delete(org.mockito.ArgumentMatchers.any(Registration.class));
    }

    @Test
    void bookingGoneAfterLockChangesNothing() {
        Event event = bookingFor(1L, 42L, LocalDateTime.now().plusDays(2));
        Registration reg = registrations.findWithDetailsById(42L).orElseThrow();
        when(registrations.findWithDetailsById(42L)).thenReturn(Optional.of(reg), Optional.empty());
        RegistrationNotFoundException ex = assertThrows(RegistrationNotFoundException.class,
                () -> controller.cancel(42L, tokenFor(1L)));
        assertEquals(HttpStatus.NOT_FOUND, handler.handleRegistrationNotFound(ex).getStatusCode());
        assertEquals(4, event.getBookedSeats());
        verify(registrations, never()).delete(org.mockito.ArgumentMatchers.any(Registration.class));
    }
}
