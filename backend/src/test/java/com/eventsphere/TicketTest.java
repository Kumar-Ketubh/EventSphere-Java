package com.eventsphere;

import com.eventsphere.controller.TicketController;
import com.eventsphere.dto.TicketResponse;
import com.eventsphere.entity.Event;
import com.eventsphere.util.IcsBuilder;
import com.eventsphere.entity.Registration;
import com.eventsphere.entity.User;
import com.eventsphere.exception.GlobalExceptionHandler;
import com.eventsphere.exception.RegistrationForbiddenException;
import com.eventsphere.exception.RegistrationNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** The ticket endpoint: owner sees it with the code, anyone else is refused. */
class TicketTest {

    private final RegistrationRepository registrations = mock(RegistrationRepository.class);
    private final TicketController controller = new TicketController(
            new RegistrationServiceImpl(mock(UserRepository.class), mock(EventRepository.class), registrations));
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    private static Jwt tokenFor(long userId) {
        return new Jwt("t", Instant.now(), Instant.now().plusSeconds(60),
                Map.of("alg", "HS256"), Map.of("sub", String.valueOf(userId)));
    }

    private void bookingFor(long userId, long registrationId) {
        User user = User.builder().id(userId).name("Asha Rao").rollNumber("1024030310").build();
        Event event = Event.builder().id(5L).title("Hack Night").venue("Hall A")
                .startTime(LocalDateTime.of(2030, 1, 2, 18, 0))
                .endTime(LocalDateTime.of(2030, 1, 2, 21, 0)).build();
        Registration reg = Registration.builder().id(registrationId).user(user).event(event)
                .registeredAt(LocalDateTime.now()).build();
        when(registrations.findWithDetailsById(registrationId)).thenReturn(Optional.of(reg));
    }

    @Test
    void ownerGetsTicketWithBookingCode() {
        bookingFor(1L, 42L);
        TicketResponse t = controller.getTicket(42L, tokenFor(1L)).getBody();
        assertEquals("ES-0042", t.bookingCode());
        assertEquals("Hack Night", t.eventTitle());
        assertEquals("Hall A", t.eventVenue());
        assertEquals("Asha Rao", t.studentName());
        assertEquals("1024030310", t.rollNumber());
    }

    @Test
    void otherStudentsBookingIsForbidden() {
        bookingFor(1L, 42L);
        RegistrationForbiddenException ex = assertThrows(RegistrationForbiddenException.class,
                () -> controller.getTicket(42L, tokenFor(2L)));
        assertEquals(HttpStatus.FORBIDDEN, handler.handleRegistrationForbidden(ex).getStatusCode());
    }

    @Test
    void unknownBookingIsNotFound() {
        when(registrations.findWithDetailsById(anyLong())).thenReturn(Optional.empty());
        RegistrationNotFoundException ex = assertThrows(RegistrationNotFoundException.class,
                () -> controller.getTicket(999L, tokenFor(1L)));
        assertEquals(HttpStatus.NOT_FOUND, handler.handleRegistrationNotFound(ex).getStatusCode());
    }

    @Test
    void ownerDownloadsCalendarFile() {
        bookingFor(1L, 42L);
        var res = controller.getCalendar(42L, tokenFor(1L));
        assertTrue(res.getHeaders().getContentType().toString().startsWith("text/calendar"));
        assertTrue(res.getHeaders().getContentDisposition().toString().contains("ES-0042.ics"));
        String ics = new String(res.getBody(), java.nio.charset.StandardCharsets.UTF_8);
        assertTrue(ics.startsWith("BEGIN:VCALENDAR\r\n"));
        assertTrue(ics.endsWith("END:VCALENDAR\r\n"));
        assertTrue(ics.contains("\r\nUID:ES-0042@eventsphere\r\n"));
        assertTrue(ics.contains("\r\nDTSTART:20300102T180000\r\n"));
        assertTrue(ics.contains("\r\nDTEND:20300102T210000\r\n"));
        assertTrue(ics.contains("\r\nSUMMARY:Hack Night\r\n"));
        assertTrue(ics.contains("\r\nLOCATION:Hall A\r\n"));
        assertTrue(ics.contains("\r\nDTSTAMP:"));
        assertFalse(ics.replace("\r\n", "").contains("\n"));
    }

    @Test
    void calendarTextIsEscaped() {
        TicketResponse t = new TicketResponse(1L, "ES-0001", 1L, "Talk, Q&A; day\\1", "Hall A\nWing",
                LocalDateTime.of(2030, 1, 2, 18, 0), LocalDateTime.of(2030, 1, 2, 21, 0), "A", null);
        String ics = IcsBuilder.build(t, java.time.Instant.now());
        assertTrue(ics.contains("SUMMARY:Talk\\, Q&A\\; day\\\\1\r\n"));
        assertTrue(ics.contains("LOCATION:Hall A\\nWing\r\n"));
    }

    @Test
    void otherStudentsCalendarIsForbidden() {
        bookingFor(1L, 42L);
        assertThrows(RegistrationForbiddenException.class, () -> controller.getCalendar(42L, tokenFor(2L)));
    }

    @Test
    void calendarStampIsRealUtc() {
        TicketResponse t = new TicketResponse(1L, "ES-0001", 1L, "Talk", "Hall A",
                LocalDateTime.of(2030, 1, 2, 18, 0), LocalDateTime.of(2030, 1, 2, 21, 0), "A", null);
        String ics = IcsBuilder.build(t, java.time.Instant.parse("2030-01-02T13:40:05Z"));
        assertTrue(ics.contains("\r\nDTSTAMP:20300102T134005Z\r\n"));
    }

    @Test
    void longCalendarLinesAreFoldedAt75Octets() {
        String title = "A".repeat(100);
        TicketResponse t = new TicketResponse(1L, "ES-0001", 1L, title, "Hall A",
                LocalDateTime.of(2030, 1, 2, 18, 0), LocalDateTime.of(2030, 1, 2, 21, 0), "A", null);
        String ics = IcsBuilder.build(t, java.time.Instant.now());
        for (String line : ics.split("\r\n")) {
            assertTrue(line.getBytes(java.nio.charset.StandardCharsets.UTF_8).length <= 75);
        }
        assertTrue(ics.replace("\r\n ", "").contains("SUMMARY:" + title + "\r\n"));
    }
}
