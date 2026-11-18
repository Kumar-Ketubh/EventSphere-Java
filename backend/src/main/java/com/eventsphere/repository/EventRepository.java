package com.eventsphere.repository;

import com.eventsphere.entity.Event;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Event} entity.
 *
 * <p>Contains queries for dynamic event categorization based on current time,
 * and a pessimistic-write-locked finder used exclusively during seat registration
 * to prevent overbooking (see ADR-002 in DECISIONS.md).</p>
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Fetches an event by ID with a PESSIMISTIC_WRITE database lock.
     *
     * <p>Translates to {@code SELECT ... FOR UPDATE} at the SQL level.
     * Any concurrent transaction attempting to lock the same row will
     * block until this transaction commits or rolls back.</p>
     *
     * @param id the event ID
     * @return an {@link Optional} containing the locked Event, or empty if not found
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT e FROM Event e WHERE e.id = :id")
    Optional<Event> findByIdWithLock(@Param("id") Long id);

    /**
     * Retrieves all LIVE events: currently happening right now.
     * Condition: startTime <= current_time <= endTime
     */
    List<Event> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Retrieves all UPCOMING events: not yet started.
     * Condition: startTime > current_time
     */
    List<Event> findByStartTimeAfter(LocalDateTime startTime);

    /**
     * Retrieves all PREVIOUS events: already ended.
     * Condition: endTime < current_time
     */
    List<Event> findByEndTimeBefore(LocalDateTime endTime);

    /**
     * Explicit JPQL query for live events (ordered by startTime ascending).
     */
    @Query("SELECT e FROM Event e WHERE e.startTime <= :now AND e.endTime >= :now ORDER BY e.startTime ASC")
    List<Event> findLiveEvents(@Param("now") LocalDateTime now);

    /**
     * Explicit JPQL query for upcoming events (ordered by startTime ascending).
     */
    @Query("SELECT e FROM Event e WHERE e.startTime > :now ORDER BY e.startTime ASC")
    List<Event> findUpcomingEvents(@Param("now") LocalDateTime now);

    /**
     * Explicit JPQL query for previous events (ordered by endTime descending).
     */
    @Query("SELECT e FROM Event e WHERE e.endTime < :now ORDER BY e.endTime DESC")
    List<Event> findPreviousEvents(@Param("now") LocalDateTime now);
}
