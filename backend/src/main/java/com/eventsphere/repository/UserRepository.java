package com.eventsphere.repository;

import com.eventsphere.entity.User;

import java.util.Optional;

/**
 * Repository interface for {@link User} entity operations.
 */
public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User save(User user);

    long count();
}
