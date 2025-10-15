package com.buy01.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

// DTO for updating an existing product - what the client sends in the request body
public class ProductUpdateDTO {
    private String name;
    private String description;

    @NotNull(message = "Price is required")
    @Min(value = 1, message = "Price must be at least 1")
    @Max(value = 100000, message = "Price cannot exceed 100000")
    private Double price;

    public ProductUpdateDTO() {}

    public String getName() { return name; }
    public String getDescription() { return description; }
    public Double getPrice() { return price; }

    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setPrice(Double price) { this.price = price; }
}

