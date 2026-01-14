package com.buy01.user.dto;

import java.util.List;

// Product response class to send product details in responses - what the user sees after adding a product
public class ProductDTO {
    private String productId;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String userId;
    private List<String> images;
    private Boolean isProductOwner;

    public ProductDTO() {
    }

    public ProductDTO(String productId, String name, String description, double price, int quantity, String userId,
            List<String> images, Boolean isProductOwner) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.userId = userId;
        this.images = images;
        this.isProductOwner = isProductOwner;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getuserId() {
        return userId;
    }

    public void setuserId(String userId) {
        this.userId = userId;
    }

    public List<String> getImages() {
        return images;
    }

    public void setImages(List<String> images) {
        this.images = images;
    }

    public Boolean getIsProductOwner() {
        return isProductOwner;
    }

    public void setIsProductOwner(Boolean isProductOwner) {
        this.isProductOwner = isProductOwner;
    }

}
