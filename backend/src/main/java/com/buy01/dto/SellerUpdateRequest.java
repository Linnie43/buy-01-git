package com.buy01.dto;

import org.springframework.web.multipart.MultipartFile;

public class SellerUpdateRequest extends UserUpdateRequest{
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
