package com.eventsphere.util;

import com.eventsphere.dto.TicketResponse;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

/** Builds a standard iCalendar (RFC 5545) file for one booked event. */
public final class IcsBuilder {

    private static final DateTimeFormatter LOCAL = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");
    private static final DateTimeFormatter UTC = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

    private IcsBuilder() {
    }

    public static String build(TicketResponse t, Instant now) {
        String[] lines = {
                "BEGIN:VCALENDAR",
                "VERSION:2.0",
                "PRODID:-//EventSphere//Calendar//EN",
                "CALSCALE:GREGORIAN",
                "BEGIN:VEVENT",
                "UID:" + t.bookingCode() + "@eventsphere",
                "DTSTAMP:" + UTC.format(now.atOffset(ZoneOffset.UTC)),
                "DTSTART:" + t.eventStartTime().format(LOCAL),
                "DTEND:" + t.eventEndTime().format(LOCAL),
                "SUMMARY:" + escape(t.eventTitle()),
                "LOCATION:" + escape(t.eventVenue()),
                "END:VEVENT",
                "END:VCALENDAR"};
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            out.append(fold(line)).append("\r\n");
        }
        return out.toString();
    }

    /** Folds a line longer than 75 octets: CRLF plus one space before each continuation (RFC 5545). */
    static String fold(String line) {
        StringBuilder out = new StringBuilder();
        int octets = 0;
        int limit = 75;
        for (int i = 0; i < line.length(); ) {
            int cp = line.codePointAt(i);
            String ch = new String(Character.toChars(cp));
            int size = ch.getBytes(StandardCharsets.UTF_8).length;
            if (octets + size > limit) {
                out.append("\r\n ");
                octets = 0;
                limit = 74;
            }
            out.append(ch);
            octets += size;
            i += ch.length();
        }
        return out.toString();
    }

    /** Escapes backslash, semicolon, comma and newlines as iCalendar text requires. */
    static String escape(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("\\", "\\\\").replace(";", "\\;").replace(",", "\\,")
                .replace("\r\n", "\\n").replace("\n", "\\n").replace("\r", "\\n");
    }
}
