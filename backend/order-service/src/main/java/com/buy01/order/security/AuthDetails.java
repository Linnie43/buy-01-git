package com.buy01.order.security;

public class AuthDetails {

    private String currentUserId;
    private String role;

    public AuthDetails(String currentUserId, String role) {
        this.currentUserId = currentUserId;
        this.role = role;
    }

    public String getCurrentUserId() {return currentUserId;}
    public void setCurrentUserId(String currentUserId) {this.currentUserId = currentUserId;}
    public String getRole() {return role;}
    public void setRole(String role) {this.role = role;}
}
