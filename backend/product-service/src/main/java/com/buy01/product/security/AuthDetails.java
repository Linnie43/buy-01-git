package com.buy01.product.security;

import com.buy01.product.model.Role;

public class AuthDetails {

    private String currentUserId;
    private Role role;

    public AuthDetails(String currentUserId, Role role) {
        this.currentUserId = currentUserId;
        this.role = role;
    }

    public String getCurrentUserId() {return currentUserId;}
    public void setCurrentUserId(String currentUserId) {this.currentUserId = currentUserId;}
    public Role getRole() {return role;}
    public void setRole(Role role) {this.role = role;}
}

