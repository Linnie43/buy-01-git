package com.buy01.order.dto;

import java.util.List;

public class CartResponseDTO {
    private String cartId;
    private List<ItemDTO> items;
    private double totalPrice;

    public CartResponseDTO() {}
    public CartResponseDTO(String cartId, List<ItemDTO> items, double totalPrice) {
        this.cartId = cartId;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public String getCartId() {return this.cartId;}
    public void setCartId(String cartId) {this.cartId = cartId;}

    public List<ItemDTO> getItems() {return this.items;}
    public void setItems(List<ItemDTO> items) {this.items = items;}

    public double getTotalPrice() {return this.totalPrice;}
    public void setTotalPrice(double totalPrice) {this.totalPrice = totalPrice;}
}
