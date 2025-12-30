package com.buy01.order.dto;

import com.buy01.order.model.OrderStatus;

public class OrderUpdateRequest {

    private OrderStatus status;

    public OrderUpdateRequest() {}
    public OrderUpdateRequest(OrderStatus status) {
        this.status = status;
    }

    public OrderStatus getStatus() {return status;}
    public void setStatus(OrderStatus status) {this.status = status;}

}