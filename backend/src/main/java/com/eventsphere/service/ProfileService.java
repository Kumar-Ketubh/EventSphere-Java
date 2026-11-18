package com.eventsphere.service;

import com.eventsphere.dto.ProfileResponse;

/**
 * Service interface for user profile operations.
 * Implementation: {@link com.eventsphere.service.impl.ProfileServiceImpl}
 */
public interface ProfileService {

    /**
     * Retrieves the user's profile data together with their registration history.
     *
     * @param userId the ID of the user
     * @return a {@link ProfileResponse} containing user details and all registrations
     */
    ProfileResponse getProfileData(Long userId);
}
