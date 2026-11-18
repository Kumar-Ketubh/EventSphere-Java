package com.eventsphere.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a college event in EventSphere.
 *
 * <p>Dynamic categorization is computed at query time using {@code LocalDateTime.now()}
 * as the reference point — see {@link com.eventsphere.repository.EventRepository}.</p>
 */
@Entity
@Table(name = "events")
public class Event {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Short title displayed on the event card. */
    @Column(nullable = false)
    private String title;

    /** Full description of the event. */
    @Column(length = 1000)
    private String description;

    /** Physical or virtual venue name. */
    @Column(nullable = false)
    private String venue;

    /** Event start date-time. Used for categorization: live / upcoming. */
    @Column(nullable = false)
    private LocalDateTime startTime;

    /** Event end date-time. Used for categorization: live / previous. */
    @Column(nullable = false)
    private LocalDateTime endTime;

    /** Maximum number of seats available for this event. */
    @Column(nullable = false)
    private int totalSeats;

    /**
     * Number of seats already booked.
     * Incremented atomically under pessimistic write lock during registration.
     */
    @Column(nullable = false)
    private int bookedSeats;

    public Event() {
    }

    public Event(Long id, String title, String description, String venue, LocalDateTime startTime, LocalDateTime endTime, int totalSeats, int bookedSeats) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.venue = venue;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSeats = totalSeats;
        this.bookedSeats = bookedSeats;
    }

    public static EventBuilder builder() {
        return new EventBuilder();
    }

    public static class EventBuilder {
        private Long id;
        private String title;
        private String description;
        private String venue;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private int totalSeats;
        private int bookedSeats;

        public EventBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public EventBuilder title(String title) {
            this.title = title;
            return this;
        }

        public EventBuilder description(String description) {
            this.description = description;
            return this;
        }

        public EventBuilder venue(String venue) {
            this.venue = venue;
            return this;
        }

        public EventBuilder startTime(LocalDateTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public EventBuilder endTime(LocalDateTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public EventBuilder totalSeats(int totalSeats) {
            this.totalSeats = totalSeats;
            return this;
        }

        public EventBuilder bookedSeats(int bookedSeats) {
            this.bookedSeats = bookedSeats;
            return this;
        }

        public Event build() {
            return new Event(id, title, description, venue, startTime, endTime, totalSeats, bookedSeats);
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
}
