package com.eventsphere.dto;

import java.time.LocalDateTime;

/**
 * DTO for a single registration record returned in profile responses.
 */
public class RegistrationResponse {
    private Long registrationId;
    private Long eventId;
    private String eventTitle;
    private String eventVenue;
    private LocalDateTime eventStartTime;
    private LocalDateTime eventEndTime;
    private LocalDateTime registeredAt;

    public RegistrationResponse() {
    }

    public RegistrationResponse(Long registrationId, Long eventId, String eventTitle, String eventVenue, LocalDateTime eventStartTime, LocalDateTime eventEndTime, LocalDateTime registeredAt) {
        this.registrationId = registrationId;
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.eventVenue = eventVenue;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
        this.registeredAt = registeredAt;
    }

    public static RegistrationResponseBuilder builder() {
        return new RegistrationResponseBuilder();
    }

    public static class RegistrationResponseBuilder {
        private Long registrationId;
        private Long eventId;
        private String eventTitle;
        private String eventVenue;
        private LocalDateTime eventStartTime;
        private LocalDateTime eventEndTime;
        private LocalDateTime registeredAt;

        public RegistrationResponseBuilder registrationId(Long registrationId) {
            this.registrationId = registrationId;
            return this;
        }

        public RegistrationResponseBuilder eventId(Long eventId) {
            this.eventId = eventId;
            return this;
        }

        public RegistrationResponseBuilder eventTitle(String eventTitle) {
            this.eventTitle = eventTitle;
            return this;
        }

        public RegistrationResponseBuilder eventVenue(String eventVenue) {
            this.eventVenue = eventVenue;
            return this;
        }

        public RegistrationResponseBuilder eventStartTime(LocalDateTime eventStartTime) {
            this.eventStartTime = eventStartTime;
            return this;
        }

        public RegistrationResponseBuilder eventEndTime(LocalDateTime eventEndTime) {
            this.eventEndTime = eventEndTime;
            return this;
        }

        public RegistrationResponseBuilder registeredAt(LocalDateTime registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public RegistrationResponse build() {
            return new RegistrationResponse(registrationId, eventId, eventTitle, eventVenue, eventStartTime, eventEndTime, registeredAt);
        }
    }

    public Long getRegistrationId() {
        return registrationId;
    }

    public void setRegistrationId(Long registrationId) {
        this.registrationId = registrationId;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getEventTitle() {
        return eventTitle;
    }

    public void setEventTitle(String eventTitle) {
        this.eventTitle = eventTitle;
    }

    public String getEventVenue() {
        return eventVenue;
    }

    public void setEventVenue(String eventVenue) {
        this.eventVenue = eventVenue;
    }

    public LocalDateTime getEventStartTime() {
        return eventStartTime;
    }

    public void setEventStartTime(LocalDateTime eventStartTime) {
        this.eventStartTime = eventStartTime;
    }

    public LocalDateTime getEventEndTime() {
        return eventEndTime;
    }

    public void setEventEndTime(LocalDateTime eventEndTime) {
        this.eventEndTime = eventEndTime;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
