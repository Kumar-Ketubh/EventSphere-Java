package com.eventsphere.repository;

import com.eventsphere.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository for {@link User} entity.
 *
 * <p>Provides CRUD operations via {@link JpaRepository} and a
 * custom finder for login/auth lookups by email address.</p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique email address.
     * Used by {@link com.eventsphere.service.impl.AuthServiceImpl} during login.
     *
     * @param email the email address to search for
     * @return an {@link Optional} containing the User, or empty if not found
     */
    Optional<User> findByEmail(String email);
}
