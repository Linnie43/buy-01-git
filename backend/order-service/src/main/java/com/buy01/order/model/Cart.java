package com.buy01.order.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.ReadOnlyProperty;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;

@Document(collection = "carts")
public class Cart {
    @Id
    private String id;
    @NotBlank
    private String userId;
    @Valid
    private List<OrderItem> items;
    @Positive
    private double totalPrice;
    @NotNull
    @Valid
    private CartStatus cartStatus;
    @CreatedDate
    private Date createTime;
    @LastModifiedDate
    private Date updateTime;
    @NotNull
    private Date expiryTime;

    public Cart(){}

    public Cart(String userId, List<OrderItem> items, double totalPrice, CartStatus cartStatus) {
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.cartStatus = cartStatus;
        this.createTime = new Date();
        this.updateTime = new Date();
        this.expiryTime = new Date(this.createTime.getTime() + (15 * 60 * 1000)); // 15 minutes from creation
    }

    protected Cart(String id, String userId, List<OrderItem> items, double totalPrice, CartStatus cartStatus) {
        this.id = id;
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
        this.cartStatus = cartStatus;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    public String getId() {return id;}

    public String getUserId() {return userId;}
    public void setUserId(String userId) {this.userId = userId;}

    public List<OrderItem> getItems() {return items;}
    public void setItems(List<OrderItem> items) {this.items = items;}

    public double getTotalPrice() {return totalPrice;}
    public void setTotalPrice(double totalPrice) {this.totalPrice = totalPrice;}

    public CartStatus getCartStatus() {return cartStatus;}
    public void setCartStatus(CartStatus cartStatus) {this.cartStatus = cartStatus;}

    public Date getCreateTime() {return createTime;}
    public void setCreateTime(Date createTime) {this.createTime = createTime;}

    public Date getUpdateTime() {return updateTime;}
    public void setUpdateTime(Date updateTime) {this.updateTime = updateTime;}

    public Date getExpiryTime() {return expiryTime;}
}
