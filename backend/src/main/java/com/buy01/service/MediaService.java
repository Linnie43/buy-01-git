package com.buy01.service;

import com.buy01.exception.ForbiddenException;
import com.buy01.exception.NotFoundException;
import com.buy01.model.Product;
import com.buy01.repository.MediaRepository;
import com.buy01.exception.FileUploadException;
import com.buy01.repository.ProductRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import com.buy01.model.Media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service
public class MediaService {
    private final MediaRepository mediaRepository;
    private final ProductRepository productRepository;

    public MediaService(MediaRepository mediaRepository, ProductRepository productRepository) throws IOException {
        this.mediaRepository = mediaRepository;
        this.productRepository = productRepository;
        Path storagePath = Paths.get("uploads").toAbsolutePath().normalize();
        Files.createDirectories(storagePath);
    }

    public Media saveImage(MultipartFile file, String productId, String userId) {
        // validating that product exists and product ownership
        Product product = productRepository.findById(productId)
        .orElseThrow(() -> new NotFoundException("Product not found"));
        if (!product.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not the product owner");
        }
        validateFile(file);

        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + "." + extension;
        String path = storeFile(file, "uploads/", fileName); // making sure all unique filenames

        Media media = new Media();
        media.setPath(path);
        media.setProductId(productId);

        return mediaRepository.save(media);


    }

    public void deleteMedia(@PathVariable String id, String userId) {
        // validate that media exists
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Media not found"));

        // validate ownership
        Product product = productRepository.findById(media.getProductId())
                .orElseThrow(() -> new NotFoundException("Product not found"));

        if (!product.getUserId().equals(userId)) {
            throw new ForbiddenException("You are not the product owner");
        }

        // delete
        mediaRepository.deleteById(id);
    }

    public String saveUserAvatar(MultipartFile file) {
        validateFile(file);
        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + "." + extension;
        return storeFile(file, "uploads/avatar/", fileName);
    }

    private String storeFile(MultipartFile file, String directory, String filename) {
        validateFile(file);
        try {
            Files.createDirectories(Paths.get(directory));
            Path filePath = Paths.get(directory).resolve(filename);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            return filePath.toString();
        } catch (IOException e) {
            throw new FileUploadException("Failed to store file", e);
        }
    }


    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new FileUploadException("File is empty");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new FileUploadException("File too large, max 2MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileUploadException("Invalid file type");
        }
    }

}
