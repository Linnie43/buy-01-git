package com.buy01.dto;

// Product response class to send product details in responses - what the user sees after adding a product
public class ProductResponse {
    private String productId;
    private String name;
    private String description;
    private double price;
    private String sellerName;

    public ProductResponse() {}

    public ProductResponse(String productId, String name, String description, double price, String sellerName) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.sellerName = sellerName;
    }

    public String getProductId() { return productId; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }
    public String getSellerName() { return sellerName; }

    public void setProductId(String productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Double price) { this.price = price; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
}
