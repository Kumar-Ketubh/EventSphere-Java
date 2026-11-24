package com.eventsphere.dto;

/**
 * Returned by login and sign-up: the signed token plus the user's public details.
 */
public record AuthResponse(String token, UserResponse user) {
}
