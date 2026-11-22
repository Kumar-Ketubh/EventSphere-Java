package com.eventsphere.repository;

import com.eventsphere.entity.Registration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Spring Data JPA repository for {@link Registration} entity.
 *
 * <p>Provides existence checks for duplicate prevention and
 * profile data retrieval with eager event/user join fetches.</p>
 */
@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    /**
     * Checks whether a registration already exists for the given user-event pair.
     * Used in {@link com.eventsphere.service.impl.RegistrationServiceImpl} to
     * enforce the unique-registration constraint before attempting to book.
     *
     * @param userId  the user's ID
     * @param eventId the event's ID
     * @return {@code true} if the user is already registered for the event
     */
    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    /**
     * Retrieves all registrations for a user with joined event data.
     * Used by {@link com.eventsphere.service.impl.ProfileServiceImpl} to
     * populate the profile page's registration history table.
     *
     * @param userId the user's ID
     * @return list of registrations with event details loaded
     */
    @Query("SELECT r FROM Registration r JOIN FETCH r.event WHERE r.user.id = :userId ORDER BY r.registeredAt DESC")
    List<Registration> findByUserId(@Param("userId") Long userId);

    /** One booking with its event and user loaded, for the ticket page. */
    @Query("SELECT r FROM Registration r JOIN FETCH r.event JOIN FETCH r.user WHERE r.id = :id")
    java.util.Optional<Registration> findWithDetailsById(@Param("id") Long id);
}
