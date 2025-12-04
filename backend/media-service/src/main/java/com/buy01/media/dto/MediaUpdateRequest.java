package com.buy01.media.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

public class MediaUpdateRequest {

    @Size(min = 0, max = 5, message = "You can delete between 0 and 5 images.")
    private List<String> imagesToDelete = new ArrayList<>();

    @Size(max = 5, message = "You can upload up to 5 new images.")
    private List<MultipartFile> newImages = new ArrayList<>();

    public MediaUpdateRequest() {}
    public MediaUpdateRequest(List<String> imagesToDelete, List<MultipartFile> newImages) {
        this.imagesToDelete = imagesToDelete;
        this.newImages = newImages;
    }

    public List<String> getImagesToDelete() { return imagesToDelete; }
    public void setImagesToDelete(List<String> imagesToDelete) { this.imagesToDelete = imagesToDelete; }

    public List<MultipartFile> getNewImages() { return newImages; }
    public void setNewImages(List<MultipartFile> newImages) { this.newImages = newImages; }
}
