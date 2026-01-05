package com.buy01.order.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document(collection = "products")
public class Product {
    @Id
    private String productId;
    private String name;
    private String description;
    private Double price;
    private int quantity;
    private String userId;
    private Date createTime;
    private Date updateTime;

    // constructor - both constructors are needed, empty one for Spring Data and one with parameters for creating objects
    public Product() {}
    public Product(String name, String description, double price, int quantity, String userId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    // Testing purpose constructor
    protected Product(String productId, String name, String description, double price, int quantity, String userId) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
    }

    // getters and setters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public Date getCreateTime() { return createTime; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public Date getUpdateTime() { return updateTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }

}


