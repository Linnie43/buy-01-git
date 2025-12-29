package com.buy01.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MediaCreateDTO {
    @NotEmpty(message = "At least one file is required")
    private List<MultipartFile> images;
    @NotBlank
    private String productId;

    public List<MultipartFile> getImages() { return images; }
    public void setImages(List<MultipartFile> images) { this.images = images; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

}
