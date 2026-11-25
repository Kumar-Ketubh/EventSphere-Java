package com.eventsphere.repository;

import com.eventsphere.entity.Registration;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for {@link Registration} entity operations.
 */
public interface RegistrationRepository {

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    List<Registration> findByUserId(Long userId);

    Optional<Registration> findWithDetailsById(Long id);

    Optional<Registration> findById(Long id);

    Registration save(Registration registration);

    void delete(Registration registration);

    long count();
}
