package com.buy01.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class AvatarUpdateRequest {
    @NotNull
    private String oldAvatar;
    @NotNull
    private MultipartFile newAvatar;

    public AvatarUpdateRequest() {}
    public AvatarUpdateRequest(String oldAvatar, MultipartFile newAvatar) {
        this.oldAvatar = oldAvatar;
        this.newAvatar = newAvatar;
    }

    public String getOldAvatar() {
        return oldAvatar;
    }
    public void setOldAvatar(String oldAvatar) {
        this.oldAvatar = oldAvatar;
    }
    public MultipartFile getNewAvatar() {
        return newAvatar;
    }
    public void setNewAvatar(MultipartFile newAvatar) {
        this.newAvatar = newAvatar;
    }

}
