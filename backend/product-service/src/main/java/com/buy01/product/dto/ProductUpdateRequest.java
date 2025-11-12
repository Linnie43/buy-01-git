package com.buy01.product.dto;

import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

// DTO for updating an existing product - what the client sends in the request body
public class ProductUpdateRequest {

    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Product name can only contain letters, numbers, and spaces")
    private String name;

    @Size(max = 500, message = "Description can be at most 500 characters")
    private String description;

    @Min(value = 1, message = "Price must be at least 1")
    @Max(value = 100000, message = "Price cannot exceed 100000")
    private Double price;

    @PositiveOrZero(message = "Quantity cannot be negative")
    private Integer quantity;

    private List<String> deletedImageIds;

    @Size(max = 5, message = "You can upload up to 5 images")
    private List<MultipartFile> images;

    private String userId;

    public ProductUpdateRequest() {}
    public ProductUpdateRequest(String name, String description, Double price, Integer quantity, List<String> deletedImageIds, List<MultipartFile> images, String userId) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
        this.deletedImageIds = deletedImageIds;
        this.images = images;
        this.userId = userId;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Double getPrice() { return price; }
    public void setPrice(Double price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public List<String> getDeletedImageIds() { return deletedImageIds; }
    public void setDeletedImageIds(List<String> deletedImageIds) { this.deletedImageIds = deletedImageIds; }

    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
}

