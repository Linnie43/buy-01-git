package com.buy01.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class AvatarCreateDTO {

    private MultipartFile avatar;

    public AvatarCreateDTO() {}
    public AvatarCreateDTO(MultipartFile avatar) {
        this.avatar = avatar;
    }

    public MultipartFile getAvatar() { return avatar; }
    public void setAvatar(MultipartFile avatar) { this.avatar = avatar; }


}
