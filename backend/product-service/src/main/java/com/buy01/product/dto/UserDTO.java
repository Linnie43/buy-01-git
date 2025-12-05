package com.buy01.product.dto;

public class UserDTO {
    private String id;
    private String role;

    public UserDTO() {}

    public UserDTO(String id, String role) {
        this.id = id;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getRole() {
        return role;
    }
}
