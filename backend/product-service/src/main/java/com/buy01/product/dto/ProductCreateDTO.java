package com.buy01.product.dto;

import jakarta.validation.constraints.*;

// DTO for creating a new product - what the client sends in the request body
public class ProductCreateDTO {

    @NotBlank(message = "Name cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Product name can only contain letters, numbers, and spaces")
    private String name;

    @Size(max = 500, message = "Description can be at most 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 1, message = "Price must be at least 1")
    @Max(value = 100000, message = "Price cannot exceed 100000")
    private Double price;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantity;

    private String userId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
