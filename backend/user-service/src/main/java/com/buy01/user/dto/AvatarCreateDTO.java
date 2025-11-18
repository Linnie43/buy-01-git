package com.buy01.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

public class AvatarCreateDTO {

    @NotEmpty(message = "At least one file is required")
    private MultipartFile avatar;
    @NotBlank
    private String userId;

    public AvatarCreateDTO() {}
    public AvatarCreateDTO(MultipartFile avatar, String userId) {
        this.avatar = avatar;
        this.userId = userId;
    }

    public MultipartFile getAvatar() { return avatar; }
    public void setAvatar(MultipartFile avatar) { this.avatar = avatar; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

}
