package com.buy01.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class AvatarCreateDTO {

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
