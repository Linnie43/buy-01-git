package com.buy01.dto;

import com.buy01.model.Role;
import org.springframework.web.multipart.MultipartFile;

// Admin's user response class extending UserResponse to include userId
public class SellerCreateDTO extends UserCreateDTO {
    private MultipartFile avatar;

    public SellerCreateDTO() {}
    public SellerCreateDTO(String firstname, String lastname, String email, String password, Role role, MultipartFile avatar) {
        super(firstname, lastname, email, password, role);
        this.avatar = avatar;
    }

    public MultipartFile getAvatar() { return avatar; }
    public void setAvatar(MultipartFile avatar) { this.avatar = avatar; }
}
