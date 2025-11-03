package com.buy01.media.dto;

import jakarta.validation.constraints.NotEmpty;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public class MediaCreateDTO {
    @NotEmpty(message = "At least one file is required")
    private List<MultipartFile> files;

    private String productId;

    public MediaCreateDTO() {}
    public MediaCreateDTO(List<MultipartFile> files, String productId) {
        this.files = files;
        this.productId = productId;
    }

    public List<MultipartFile> getFiles() { return files; }
    public void setFiles(List<MultipartFile> files) { this.files = files; }

    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

}
