package com.eventsphere.service.impl;

import com.eventsphere.dto.ProfileResponse;
import com.eventsphere.dto.RegistrationResponse;
import com.eventsphere.dto.UserResponse;
import com.eventsphere.entity.Registration;
import com.eventsphere.entity.User;
import com.eventsphere.exception.UserNotFoundException;
import com.eventsphere.repository.RegistrationRepository;
import com.eventsphere.repository.UserRepository;
import com.eventsphere.service.ProfileService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ProfileService} aggregating user and registration data.
 */
@Service
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;

    public ProfileServiceImpl(UserRepository userRepository,
                              RegistrationRepository registrationRepository) {
        this.userRepository = userRepository;
        this.registrationRepository = registrationRepository;
    }

    @Override
    public ProfileResponse getProfileData(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        List<Registration> registrations = registrationRepository.findByUserId(userId);

        UserResponse userResponse = UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .rollNumber(user.getRollNumber())
                .build();

        List<RegistrationResponse> regResponses = registrations.stream()
                .map(reg -> RegistrationResponse.builder()
                        .registrationId(reg.getId())
                        .eventId(reg.getEvent().getId())
                        .eventTitle(reg.getEvent().getTitle())
                        .eventVenue(reg.getEvent().getVenue())
                        .eventStartTime(reg.getEvent().getStartTime())
                        .eventEndTime(reg.getEvent().getEndTime())
                        .registeredAt(reg.getRegisteredAt())
                        .build())
                .collect(Collectors.toList());

        return ProfileResponse.builder()
                .user(userResponse)
                .registrations(regResponses)
                .build();
    }
}
