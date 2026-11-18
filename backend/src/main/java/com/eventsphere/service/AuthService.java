package com.eventsphere.service;

import com.eventsphere.dto.LoginRequest;
import com.eventsphere.dto.RegisterRequest;
import com.eventsphere.dto.UserResponse;

/**
 * Service interface for authentication operations.
 * Implementation: {@link com.eventsphere.service.impl.AuthServiceImpl}
 */
public interface AuthService {

    /**
     * Registers a new user with the provided details.
     *
     * @param request registration data (name, email, password, rollNumber)
     * @return the created user's public profile data
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticates a user by email and password.
     *
     * @param request login credentials (email, password)
     * @return the authenticated user's public profile data
     * @throws com.eventsphere.exception.UserNotFoundException if email not found
     * @throws com.eventsphere.exception.BadCredentialsException if password doesn't match
     */
    UserResponse login(LoginRequest request);
}
