package com.buy01.user.dto;

import com.buy01.user.model.Role;

public class UserResponseDTO {
    private String name;
    private String email;
    private Role role;
    private String avatar;
    private Boolean ownProfile;

    public UserResponseDTO() {}
    public UserResponseDTO(String name, String email,  Role role, String avatar, Boolean ownProfile) {
        this.name = name;
        this.email = email;
        this.role = role;
        this.avatar = avatar;
        this.ownProfile = ownProfile;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }

    public Boolean getOwnProfile() { return ownProfile; }
    public void setOwnProfile(Boolean ownProfile) { this.ownProfile = ownProfile; }
}
