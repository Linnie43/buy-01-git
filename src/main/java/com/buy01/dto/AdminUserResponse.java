package com.buy01.dto;

// Admin's user response class extending UserResponse to include userId
public class AdminUserResponse extends UserResponse {
    private String userId;
    private String name;
    private String email;

    public AdminUserResponse() {}

    public AdminUserResponse(String userId, String name, String email) {
        this.userId = userId;
        this.name = name;
        this.email = email;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
}
