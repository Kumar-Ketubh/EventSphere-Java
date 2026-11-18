package com.eventsphere.dto;

/**
 * DTO for user registration request body.
 * Maps to POST /api/auth/register request payload.
 */
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private String rollNumber;

    public RegisterRequest() {
    }

    public RegisterRequest(String name, String email, String password, String rollNumber) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.rollNumber = rollNumber;
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
