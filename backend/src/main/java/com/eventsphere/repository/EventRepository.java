package com.eventsphere.repository;

import com.eventsphere.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Event} entity operations.
 */
public interface EventRepository {

    Optional<Event> findById(Long id);

    /**
     * Fetches an event by ID with a database row lock (SELECT ... FOR UPDATE).
     * Used during seat registration and cancellation to prevent race conditions.
     */
    Optional<Event> findByIdWithLock(Long id);

    List<Event> findLiveEvents(LocalDateTime now);

    List<Event> findUpcomingEvents(LocalDateTime now);

    List<Event> findPreviousEvents(LocalDateTime now);

    Event save(Event event);

    List<Event> findAll();

    long count();
}
