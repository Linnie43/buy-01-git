package com.buy01.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CartItemRequestDTO {
    @NotBlank
    private String productId;
    @Min(1)
    private Integer quantity;

    public CartItemRequestDTO() {}
    public CartItemRequestDTO(String productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public String getProductId() {return this.productId;}
    public void setProductId(String productId) {this.productId = productId;}

    public Integer getQuantity() {return this.quantity;}
    public void setQuantity(Integer quantity) {this.quantity = quantity;}
}
