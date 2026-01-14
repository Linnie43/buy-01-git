package com.buy01.product.dto;

import com.buy01.product.model.ProductCategory;

import java.util.List;

// Product response class to send product details in responses - what the user sees after adding a product
public class ProductResponseDTO {
    private String productId;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private ProductCategory category;
    private String ownerId;
    private List<String> images;
    private Boolean isProductOwner;

    public ProductResponseDTO() {}

    public ProductResponseDTO(String productId, String name, String description, double price, int quantity, ProductCategory category, String ownerId, List<String> images, Boolean isProductOwner) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
        this.ownerId = ownerId;
        this.images = images;
        this.isProductOwner = isProductOwner;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public List<String> getImages() { return images; }
    public void setImages(List<String> images) { this.images = images; }

    public Boolean getIsProductOwner() { return isProductOwner; }
    public void setIsProductOwner(Boolean isProductOwner) { this.isProductOwner = isProductOwner; }

}
