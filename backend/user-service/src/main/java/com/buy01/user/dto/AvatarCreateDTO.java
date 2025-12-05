package com.buy01.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class AvatarCreateDTO {

    @NotNull(message = "At least one file is required")
    private MultipartFile avatar;

    public AvatarCreateDTO() {}
    public AvatarCreateDTO(MultipartFile avatar) {
        this.avatar = avatar;
    }

    public MultipartFile getAvatar() { return avatar; }
    public void setAvatar(MultipartFile avatar) { this.avatar = avatar; }


}
