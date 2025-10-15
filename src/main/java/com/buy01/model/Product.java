package com.buy01.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "products")
public class Product {
    @Id
    private String productId;
    private String name;
    private String description;
    private Double price;
    private String userId;
    private String sellerName;

    // constructor - both constructors are needed, empty one for Spring Data and one with parameters for creating objects
    public Product() {}
    public Product(String name, String description, double price, String userId, String sellerName) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.userId = userId;
        this.sellerName = sellerName;
    }

    // getters and setters
    public String getProductId() { return productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

}


