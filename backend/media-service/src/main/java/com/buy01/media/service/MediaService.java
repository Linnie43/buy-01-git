package com.buy01.media.service;

import com.buy01.media.client.ProductClient;
import com.buy01.media.dto.MediaResponseDTO;
import com.buy01.media.exception.ForbiddenException;
import com.buy01.media.exception.NotFoundException;
import com.buy01.media.repository.MediaRepository;
import com.buy01.media.exception.FileUploadException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.multipart.MultipartFile;
import com.buy01.media.model.Media;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.sql.Savepoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class MediaService {
    private final MediaRepository mediaRepository;
    private final ProductClient productClient;
    private final Path storagePath;
    private final Path avatarPath;

    private static final String UPLOAD_DIR = "uploads";
    private static final String AVATAR_DIR = "avatar";

    public MediaService(MediaRepository mediaRepository, ProductClient productClient) throws IOException {
        this.mediaRepository = mediaRepository;
        this.productClient = productClient;

        this.storagePath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(storagePath);

        this.avatarPath = storagePath.resolve(AVATAR_DIR);
        Files.createDirectories(avatarPath);
    }

    // saves all images and returns result, trust validation from product service
    public List<MediaResponseDTO> saveProductImages(String productId, List<MultipartFile> files) throws IOException {

        System.out.println("Saving images for productId: " + productId + ", number of files: " + files.size());
        // Stream files, save them and create a list of MediaResponseDTO for return
        List<MediaResponseDTO> result = files.stream()
                .map(file -> {
                    Media media = saveImage(file, productId);
                    return new MediaResponseDTO(media.getId(), media.getProductId());
                })
                .toList();
        System.out.println("saved images: " + result.size());
        return result;
    }

    // creates path and saves the image to uploads
    public Media saveImage(MultipartFile file, String productId) {

        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + extension;
        String path = storeFile(file, storagePath.toString(), fileName); // making sure all unique filenames

        Media media = new Media();
        media.setPath(path);
        media.setProductId(productId);

        return mediaRepository.save(media);
    }

    // delete media from database
    public void deleteMedia(String id) {
        // get media by id
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Media not found"));

        deleteFile(media.getPath());
        mediaRepository.deleteById(id);
    }

    public String saveUserAvatar(MultipartFile file) {
        String extension = Objects.requireNonNull(file.getOriginalFilename())
                .substring(file.getOriginalFilename().lastIndexOf("."));
        String fileName = UUID.randomUUID() + "." + extension;
        return storeFile(file, avatarPath.toString(), fileName);
    }

    public void deleteAvatar(String path) {
        deleteFile(path);
    }


    // validating the file before storing
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

    // validate file type (image) and size (max 2MB)
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

    // delete file from server
    private void deleteFile(String filePathStr) {
        Path filePath = Paths.get(filePathStr).toAbsolutePath();
        try {
            boolean deleted = Files.deleteIfExists(filePath);
            if (!deleted) {
                System.out.println("File not found: " + filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete file: " + filePath, e);
        }
    }

}
