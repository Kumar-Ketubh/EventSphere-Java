package com.eventsphere.dto;

import java.time.LocalDateTime;

/**
 * DTO representing a single event in API responses.
 * Used in dashboard listings and registration confirmations.
 */
public class EventResponse {
    private Long id;
    private String title;
    private String description;
    private String venue;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalSeats;
    private int bookedSeats;

    public EventResponse() {
    }

    public EventResponse(Long id, String title, String description, String venue, LocalDateTime startTime, LocalDateTime endTime, int totalSeats, int bookedSeats) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
    }

    public static EventResponseBuilder builder() {
        return new EventResponseBuilder();
    }

    public static class EventResponseBuilder {
        private Long id;
        private String title;
        private String description;
        private String venue;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int totalSeats;
        private int bookedSeats;

        public EventResponseBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventResponseBuilder title(String title) {
            this.title = title;
            return this;
        }

        public EventResponseBuilder description(String description) {
            this.description = description;
            return this;
        }

        public EventResponseBuilder venue(String venue) {
            this.venue = venue;
            return this;
        }

        public EventResponseBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public EventResponseBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public EventResponseBuilder totalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        public EventResponseBuilder bookedSeats(int bookedSeats) {
            this.bookedSeats = bookedSeats;
            return this;
        }

        public EventResponse build() {
            return new EventResponse(id, title, description, venue, startTime, endTime, totalSeats, bookedSeats);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVenue() {
        return venue;
    }

    public void setVenue(String venue) {
        this.venue = venue;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public int getTotalSeats() {
        return totalSeats;
    }

    public void setTotalSeats(int totalSeats) {
        this.totalSeats = totalSeats;
    }

    public int getBookedSeats() {
        return bookedSeats;
    }

    public void setBookedSeats(int bookedSeats) {
        this.bookedSeats = bookedSeats;
    }

    public int getAvailableSeats() {
        return totalSeats - bookedSeats;
    }
}
