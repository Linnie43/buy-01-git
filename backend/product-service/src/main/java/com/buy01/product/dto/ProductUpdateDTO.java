package com.buy01.product.dto;

import com.buy01.product.model.ProductCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

public class ProductUpdateDTO {
    @NotBlank(message = "Product ID cannot be empty")
    private String productId;
    @NotBlank(message = "Name cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Product name can only contain letters, numbers, and spaces")
    private String name;
    @NotNull(message = "Price is required")
    @Positive(message = "Price must be over 0")
    @Max(value = 100000, message = "Price cannot exceed 100000")
    private double price;
    @NotNull(message = "Quantity is required")
    @Min(value = 0, message = "Quantity cannot be negative")
    @Max(value = 1000, message = "Quantity cannot exceed 1000")
    private int quantity;
    @NotNull
    @Valid
    private ProductCategory category;

    public ProductUpdateDTO() {}

    public ProductUpdateDTO(String productId, String name, double price, int quantity, ProductCategory category) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.category = category;
    }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public ProductCategory getCategory() { return category; }
    public void setCategory(ProductCategory category) { this.category = category; }

}
