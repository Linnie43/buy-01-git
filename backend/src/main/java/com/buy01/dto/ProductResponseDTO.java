package com.buy01.dto;

import com.buy01.model.Media;

import java.util.List;

// Product response class to send product details in responses - what the user sees after adding a product
public class ProductResponseDTO {
    private String productId;
    private String name;
    private String description;
    private double price;
    private int quantity;
    private String sellerName;
    private List<Media> images;

    public ProductResponseDTO() {}

    public ProductResponseDTO(String productId, String name, String description, double price, int quantity, String sellerName,  List<Media> images) {
        this.productId = productId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.sellerName = sellerName;
        this.images = images;
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

    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }

    public List<Media> getImages() { return images; }
    public void setImages(List<Media> images) { this.images = images; }
}
