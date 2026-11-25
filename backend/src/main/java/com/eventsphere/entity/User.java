package com.eventsphere.entity;

/**
 * Domain model representing an EventSphere user (Student / Faculty).
 *
 * <p>Stored in the {@code users} table and managed via JDBC.
 * Password is stored as plain-text for MVP demo - see ADR-004 in DECISIONS.md.</p>
 */
public class User {

    private Long id;
    private String name;
    private String email;
    private String password;
    private String rollNumber;

    public User() {
    }

    public User(Long id, String name, String email, String password, String rollNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.rollNumber = rollNumber;
    }

    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String name;
        private String email;
        private String password;
        private String rollNumber;

        public UserBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public UserBuilder name(String name) {
            this.name = name;
            return this;
        }

        public UserBuilder email(String email) {
            this.email = email;
            return this;
        }

        public UserBuilder password(String password) {
            this.password = password;
            return this;
        }

        public UserBuilder rollNumber(String rollNumber) {
            this.rollNumber = rollNumber;
            return this;
        }

        public User build() {
            return new User(id, name, email, password, rollNumber);
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public void setRollNumber(String rollNumber) {
        this.rollNumber = rollNumber;
    }
}
