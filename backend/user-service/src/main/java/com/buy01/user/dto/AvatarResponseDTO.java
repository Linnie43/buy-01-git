package com.buy01.user.dto;

public class AvatarResponseDTO {
    private String avatarUrl;

    public AvatarResponseDTO() {}
    public AvatarResponseDTO(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }
    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
