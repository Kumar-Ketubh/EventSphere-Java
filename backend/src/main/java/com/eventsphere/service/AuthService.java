package com.eventsphere.service;

import com.eventsphere.dto.AuthResponse;
import com.eventsphere.dto.LoginRequest;
import com.eventsphere.dto.RegisterRequest;

/**
 * Service interface for authentication operations.
 * Implementation: {@link com.eventsphere.service.impl.AuthServiceImpl}
 */
public interface AuthService {

    /**
     * Registers a new user with the provided details.
     *
     * @param request registration data (name, email, password, rollNumber)
     * @return a login token and the created user's public profile data
     */
    AuthResponse register(RegisterRequest request);

    /**
     * Authenticates a user by email and password.
     *
     * @param request login credentials (email, password)
     * @return a login token and the user's public profile data
     * @throws com.eventsphere.exception.UserNotFoundException if email not found
     * @throws com.eventsphere.exception.BadCredentialsException if password doesn't match
     */
    AuthResponse login(LoginRequest request);
}
