package com.eventsphere.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity representing a confirmed seat registration.
 */
@Entity
@Table(
    name = "registrations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "event_id"})
)
public class Registration {

    /** Auto-generated primary key. */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** The user who registered — many registrations can belong to one user. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /** The event the user registered for — many registrations per event. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    /** Timestamp of when the registration was created. */
    @Column(nullable = false)
    private LocalDateTime registeredAt;

    public Registration() {
    }

    public Registration(Long id, User user, Event event, LocalDateTime registeredAt) {
        this.id = id;
        this.user = user;
        this.event = event;
        this.registeredAt = registeredAt;
    }

    public static RegistrationBuilder builder() {
        return new RegistrationBuilder();
    }

    public static class RegistrationBuilder {
        private Long id;
        private User user;
        private Event event;
        private LocalDateTime registeredAt;

        public RegistrationBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public RegistrationBuilder user(User user) {
            this.user = user;
            return this;
        }

        public RegistrationBuilder event(Event event) {
            this.event = event;
            return this;
        }

        public RegistrationBuilder registeredAt(LocalDateTime registeredAt) {
            this.registeredAt = registeredAt;
            return this;
        }

        public Registration build() {
            return new Registration(id, user, event, registeredAt);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public LocalDateTime getRegisteredAt() {
        return registeredAt;
    }

    public void setRegisteredAt(LocalDateTime registeredAt) {
        this.registeredAt = registeredAt;
    }
}
