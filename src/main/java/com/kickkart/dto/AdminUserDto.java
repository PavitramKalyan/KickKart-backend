package com.kickkart.dto;

import java.time.LocalDateTime;

public class AdminUserDto {

    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String role;
    private LocalDateTime createdAt;

    public AdminUserDto() {}

    public AdminUserDto(Long userId, String username, String fullName, String email, String mobileNumber, String role, LocalDateTime createdAt) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.role = role;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
