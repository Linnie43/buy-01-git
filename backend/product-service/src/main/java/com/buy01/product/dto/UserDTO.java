package com.buy01.product.dto;

import com.buy01.product.model.Role;

public class UserDTO {
    private String id;
    private Role role;

    public UserDTO() {}

    public UserDTO(String id, Role role) {
        this.id = id;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }
}
