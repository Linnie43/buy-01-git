package com.buy01.product.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// DTO for creating a new product - what the client sends in the request body
public class ProductCreateDTO {

    @NotBlank(message = "Name cannot be empty")
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Product name can only contain letters, numbers, and spaces")
    private String name;

    @Size(max = 500, message = "Description can be at most 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be over 0")
    @Max(value = 100000, message = "Price cannot exceed 100000")
    private Double price;

    @NotNull(message = "Quantity is required")
    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantity;

    @Size(max = 5, message = "You can upload up to 5 images")
    private List<MultipartFile> imagesList;

    private String userId;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public List<MultipartFile> getImagesList() { return imagesList; }
    public void setImagesList(List<MultipartFile> imagesList) { this.imagesList = imagesList; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}
