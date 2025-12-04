package com.buy01.user.dto;

import com.buy01.user.model.User;

import java.util.List;

public class SellerResponseDTO extends UserResponseDTO {
    private List<ProductDTO> products;

    public SellerResponseDTO(User user, List<ProductDTO> products) {
        super(user.getName(), user.getEmail(), user.getRole(), user.getAvatarUrl(), true);
        this.products = products;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }
    public void setProducts(List<ProductDTO> products) {
        this.products = products;
    }
}
