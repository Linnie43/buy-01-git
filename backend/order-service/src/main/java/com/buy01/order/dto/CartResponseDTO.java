package com.buy01.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.Date;
import java.util.List;

public class CartResponseDTO {
    @NotBlank
    private String cartId;
    @Valid
    private List<ItemDTO> items;
    private double totalPrice;
    private Date expiryTime;

    public CartResponseDTO() {}
    public CartResponseDTO(String cartId, List<ItemDTO> items, double totalPrice, Date expiryTime) {
        this.cartId = cartId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.expiryTime = expiryTime;
    }

    public String getCartId() {return this.cartId;}
    public void setCartId(String cartId) {this.cartId = cartId;}

    public List<ItemDTO> getItems() {return this.items;}
    public void setItems(List<ItemDTO> items) {this.items = items;}

    public double getTotalPrice() {return this.totalPrice;}
    public void setTotalPrice(double totalPrice) {this.totalPrice = totalPrice;}

    public Date getExpiryTime() {return this.expiryTime;}
    public void setExpiryTime(Date expiryTime) {this.expiryTime = expiryTime;}
}
