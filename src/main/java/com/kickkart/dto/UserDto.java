package com.kickkart.dto;

public class UserDto {
    private Long userId;
    private String username;
    private String fullName;
    private String email;
    private String mobileNumber;
    private String profilePicture;
    private String role;

    public UserDto() {}

    public UserDto(Long userId, String username, String fullName, String email, String mobileNumber, String profilePicture, String role) {
        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.email = email;
        this.mobileNumber = mobileNumber;
        this.profilePicture = profilePicture;
        this.role = role;
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

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
