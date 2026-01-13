package com.buy01.order.dto;

import com.buy01.order.model.CartStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class CartUpdateRequest {
    @NotNull
    @Valid
    private CartStatus cartStatus;

    public CartUpdateRequest() {}
    public CartUpdateRequest(CartStatus cartStatus) {
        this.cartStatus = cartStatus;
    }

    public CartStatus getCartStatus() {return cartStatus;}
    public void setCartStatus(CartStatus cartStatus) {this.cartStatus = cartStatus;}
}
