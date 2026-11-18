package com.eventsphere.config;

import com.eventsphere.entity.Event;
import com.eventsphere.entity.User;
import com.eventsphere.repository.EventRepository;
import com.eventsphere.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Data initializer that seeds the H2 in-memory database with sample data on startup.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public DataInitializer(UserRepository userRepository, EventRepository eventRepository) {
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("DataInitializer: Data already seeded, skipping.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // ─── Seed Sample User ──────────────────────────────────────────────────────
        User student = User.builder()
                .name("Alex Johnson")
                .email("student@college.edu")
                .password("password123") // TODO: BCrypt in production (see ADR-004)
                .rollNumber("CS2024001")
                .build();
        userRepository.save(student);
        log.info("DataInitializer: Seeded sample user — student@college.edu");

        // ─── LIVE EVENT (currently happening) ─────────────────────────────────────
        eventRepository.save(Event.builder()
                .title("Annual Tech Symposium 2026")
                .description("A full-day technology symposium featuring keynotes on AI, Cloud, and Open Source. Live Q&A with industry leaders.")
                .venue("Main Auditorium, Block A")
                .startTime(now.minusHours(1))
                .endTime(now.plusHours(2))
                .totalSeats(200)
                .bookedSeats(147)
                .build());

        // ─── UPCOMING EVENT 1 (tomorrow) ──────────────────────────────────────────
        eventRepository.save(Event.builder()
                .title("Machine Learning Workshop")
                .description("Hands-on ML workshop covering supervised learning, neural networks, and model deployment using Python and scikit-learn.")
                .venue("Computer Lab 3, Block B")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(1).plusHours(4))
                .totalSeats(60)
                .bookedSeats(23)
                .build());

        // ─── UPCOMING EVENT 2 (3 days from now) ───────────────────────────────────
        eventRepository.save(Event.builder()
                .title("Entrepreneurship Summit")
                .description("Panel discussions with startup founders, venture capitalists, and innovation leads. Includes a startup pitch competition.")
                .venue("Seminar Hall 1, Administrative Block")
                .startTime(now.plusDays(3))
                .endTime(now.plusDays(3).plusHours(6))
                .totalSeats(150)
                .bookedSeats(89)
                .build());

        // ─── PREVIOUS EVENT 1 (yesterday) ─────────────────────────────────────────
        eventRepository.save(Event.builder()
                .title("Cultural Fest — Rhythms 2026")
                .description("Inter-college cultural extravaganza with dance, music, drama, and art competitions. Grand finale with celebrity performance.")
                .venue("Open Air Theatre, Campus Ground")
                .startTime(now.minusDays(1).minusHours(5))
                .endTime(now.minusDays(1))
                .totalSeats(500)
                .bookedSeats(500)
                .build());

        // ─── PREVIOUS EVENT 2 (last week) ─────────────────────────────────────────
        eventRepository.save(Event.builder()
                .title("Robotics Competition 2026")
                .description("National-level robotics competition. Teams of 4 compete in obstacle navigation, autonomous task completion, and combat rounds.")
                .venue("Engineering Workshop, Block C")
                .startTime(now.minusDays(7).minusHours(3))
                .endTime(now.minusDays(7))
                .totalSeats(80)
                .bookedSeats(76)
                .build());

        log.info("DataInitializer: Seeded 5 events (1 live, 2 upcoming, 2 previous).");
    }
}
