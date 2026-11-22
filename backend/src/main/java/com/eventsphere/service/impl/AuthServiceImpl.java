package com.eventsphere.service.impl;

import com.eventsphere.dto.AuthResponse;
import com.eventsphere.dto.LoginRequest;
import com.eventsphere.dto.RegisterRequest;
import com.eventsphere.dto.UserResponse;
import com.eventsphere.entity.User;
import com.eventsphere.exception.BadCredentialsException;
import com.eventsphere.exception.UserNotFoundException;
import com.eventsphere.repository.UserRepository;
import com.eventsphere.service.AuthService;
import com.eventsphere.service.TokenService;
import org.springframework.stereotype.Service;

/**
 * Implementation of {@link AuthService} handling user registration and login.
 */
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final TokenService tokenService;

    public AuthServiceImpl(UserRepository userRepository, TokenService tokenService) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword()) // TODO: BCrypt in production
                .rollNumber(request.getRollNumber())
                .build();

        User saved = userRepository.save(user);
        return toAuthResponse(saved);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException(
                        "No account found with email: " + request.getEmail()));

        if (!user.getPassword().equals(request.getPassword())) {
            throw new BadCredentialsException("Incorrect password. Please try again.");
        }

        return toAuthResponse(user);
    }

    private AuthResponse toAuthResponse(User user) {
        return new AuthResponse(tokenService.issue(user), mapToUserResponse(user));
    }

    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .rollNumber(user.getRollNumber())
                .build();
    }
}
