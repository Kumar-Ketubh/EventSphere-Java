package com.eventsphere.controller;

import com.eventsphere.dto.ProfileResponse;
import com.eventsphere.service.ProfileService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for user profile and registration history retrieval.
 */
@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileResponse> getProfile(@RequestParam Long userId) {
        return ResponseEntity.ok(profileService.getProfileData(userId));
    }
}
