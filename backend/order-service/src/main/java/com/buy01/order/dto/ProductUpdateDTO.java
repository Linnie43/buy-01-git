package com.buy01.order.dto;

import com.buy01.order.model.ProductCategory;
import jakarta.validation.constraints.*;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductUpdateDTO {
    @NotBlank
    private String productId;
    @NotBlank
    @JsonProperty("name")
    private String productName;
    @Positive(message = "Price must be over 0")
    @JsonProperty("price")
    private double productPrice;
    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull(message = "Quantity is required")
    private int quantity;
    private ProductCategory category;
    @JsonProperty("userId")
    private String sellerId;

    public ProductUpdateDTO() {}
    public ProductUpdateDTO(String productId, String productName, double productPrice, int quantity, ProductCategory category, String sellerId) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.category = category;
        this.sellerId = sellerId;
    }

    public String getProductId() {return productId;}
    public void setProductId(String productId) {this.productId = productId;}

    public String getProductName() {return productName;}
    public void setProductName(String productName) {this.productName = productName;}

    public double getProductPrice() {return productPrice;}
    public void setProductPrice(double productPrice) {this.productPrice = productPrice;}

    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}

    public ProductCategory getCategory() {return category;}
    public void setCategory(ProductCategory category) {this.category = category;}

    public String getSellerId() {return sellerId;}
    public void setSellerId(String sellerId) {this.sellerId = sellerId;}
}
