package com.eventsphere.dto;

import java.util.List;

/**
 * DTO wrapping user details and their registration history.
 * Returned by GET /api/profile.
 */
public class ProfileResponse {
    private UserResponse user;
    private List<RegistrationResponse> registrations;

    public ProfileResponse() {
    }

    public ProfileResponse(UserResponse user, List<RegistrationResponse> registrations) {
        this.user = user;
        this.registrations = registrations;
    }

    public static ProfileResponseBuilder builder() {
        return new ProfileResponseBuilder();
    }

    public static class ProfileResponseBuilder {
        private UserResponse user;
        private List<RegistrationResponse> registrations;

        public ProfileResponseBuilder user(UserResponse user) {
            this.user = user;
            return this;
        }

        public ProfileResponseBuilder registrations(List<RegistrationResponse> registrations) {
            this.registrations = registrations;
            return this;
        }

        public ProfileResponse build() {
            return new ProfileResponse(user, registrations);
        }
    }

    public UserResponse getUser() {
        return user;
    }

    public void setUser(UserResponse user) {
        this.user = user;
    }

    public List<RegistrationResponse> getRegistrations() {
        return registrations;
    }

    public void setRegistrations(List<RegistrationResponse> registrations) {
        this.registrations = registrations;
    }
}
