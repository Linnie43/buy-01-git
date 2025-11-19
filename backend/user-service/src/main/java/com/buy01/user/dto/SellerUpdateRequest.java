package com.buy01.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.multipart.MultipartFile;

public class SellerUpdateRequest extends com.buy01.user.dto.UserUpdateRequest {
    @NotNull
    private MultipartFile avatar;

    public SellerUpdateRequest() {}
    public SellerUpdateRequest(String name, String email, String password, MultipartFile avatar) {
        super(name, email, password);
        this.avatar = avatar;
    }

    public MultipartFile getAvatar() {
        return avatar;
    }
    public void setAvatar(MultipartFile avatar) {
        this.avatar = avatar;
    }
}
