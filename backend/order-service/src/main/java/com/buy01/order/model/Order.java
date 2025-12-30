package com.buy01.order.model;

import jakarta.validation.constraints.NotBlank;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "orders")
public class Order {
    @Id
    private String id;
    @NotBlank
    private String userId;
    private List<OrderItem> items;
    private double totalPrice;
    private OrderStatus status;
    private Date createdAt;
    private Date updatedAt;

    // constructor - both constructors are needed, empty one for Spring Data and one with parameters for creating objects
    public Order() {}
    public Order(String userId, List<OrderItem> items, double totalPrice, OrderStatus status) {
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // Testing purpose constructor
    protected Order(String id, String userId, List<OrderItem> items, double totalPrice, OrderStatus status) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.status = status;
        this.createdAt = new Date();
        this.updatedAt = new Date();
    }

    // getters and setters
    public String getId() {return id;}

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public List<OrderItem> getItems() {return items;}
    public void setItems(List<OrderItem> items) {this.items = items;}

    public double getTotalPrice() {return totalPrice;}
    public void setTotalPrice(double totalPrice) {this.totalPrice = totalPrice;}

    public OrderStatus getStatus() {return status;}
    public void setStatus(OrderStatus status) {this.status = status;}

    public Date getCreatedAt() {return createdAt;}
    public void setCreatedAt(Date createdAt) {this.createdAt = createdAt;}

    public Date getUpdatedAt() {return updatedAt;}
    public void setUpdatedAt(Date updatedAt) {this.updatedAt = updatedAt;}

}


