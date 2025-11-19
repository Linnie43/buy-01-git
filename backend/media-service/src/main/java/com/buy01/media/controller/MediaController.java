package com.buy01.media.controller;

import com.buy01.media.dto.AvatarCreateDTO;
import com.buy01.media.dto.*;
import com.buy01.media.exception.NotFoundException;
import com.buy01.media.model.Media;
import com.buy01.media.repository.MediaRepository;
import com.buy01.media.service.MediaService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final MediaRepository mediaRepository;
    private final MediaService mediaService;
    private final String avatarDir = "uploads/avatar";

    public MediaController(MediaRepository mediaRepository,MediaService mediaService) {
        this.mediaRepository = mediaRepository;
        this.mediaService = mediaService;
    }

    // uploading media to the server, validating and saving metadata to database
    @PostMapping("/internal/images")
    public ResponseEntity<List<MediaResponseDTO>> uploadImage(
            @Valid @ModelAttribute MediaCreateDTO dto
    ) throws IOException {

        List<MediaResponseDTO> result = mediaService.saveProductImages(dto.getProductId(), dto.getFiles());

        System.out.println("Responding ok from media-service: "+ result);
        return ResponseEntity.ok(result);
    }

    // serves the raw image bytes
    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> getImage(
            @PathVariable String id
    ) throws IOException {
        System.out.println("Image requested with id: "+ id);
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        Path filePath = Paths.get(media.getPath()).toAbsolutePath();
        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            System.out.println("Resource doesn't exist or is not readable");
            throw new NotFoundException("Image file not found");
        }

        System.out.println("Resource found:" + resource);

        String contentType = Files.probeContentType(filePath);
        MediaType mediaType = (contentType != null) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(resource);
    }

    // serves all URLS for productId
    @GetMapping("/internal/images/productId/{productId}")
    public List<MediaResponseDTO> getProductImages(
            @PathVariable String productId
    ) {
        System.out.println("Get product image requested with id: "+ productId);
        List<Media> mediaList = mediaRepository.getMediaByProductId(productId);

        return mediaList.stream()
                .map(media -> new MediaResponseDTO(
                        media.getId(),
                        media.getProductId()
                ))
                .toList();
    }

    // Endpoint to update images for a product
    @PutMapping("/internal/images/productId/{productId}")
    public ResponseEntity<List<MediaResponseDTO>> updateProductImages(
            @PathVariable String productId,
            @Valid @ModelAttribute MediaUpdateRequest dto
    ) throws IOException {
        List<MediaResponseDTO> updatedMedia = mediaService.updateProductImages(
                productId,
                dto.getImagesToDelete(),
                dto.getNewImages()
        );

        return ResponseEntity.ok(updatedMedia);
    }

    // Delete image as per id
    @DeleteMapping("/internal/images/{id}")
    public ResponseEntity<?> deleteImage(
            @PathVariable String id
    ) {
        mediaService.deleteMedia(id);
        return ResponseEntity.ok().build();
    }

    // /avatar endpoints

    // uploading avatar to the server, validating and returning path
    // uploading media to the server, validating and saving metadata to database
    @PostMapping("/internal/avatar")
    public ResponseEntity<AvatarResponseDTO> uploadAvatar(
            @Valid @ModelAttribute AvatarCreateDTO dto
    ) throws IOException {

        String url = mediaService.saveUserAvatar(dto.getAvatar());

        return ResponseEntity.ok(new AvatarResponseDTO(url));
    }

    // serve the avatar url from the server
    @GetMapping("/avatar/{filename}")
    public ResponseEntity<Resource> getAvatar(
            @PathVariable String filename
    ) throws IOException {
        System.out.println("Avatar requested with path: "+ filename);


        Path baseDir = Paths.get(avatarDir).toAbsolutePath().normalize();
        Path filePath = baseDir.resolve(filename).normalize();

        System.out.println("Filepath: " + filePath);

        // prevent path traversal
        if (!filePath.startsWith(baseDir)) {
            throw new NotFoundException("Invalid path");
        }

        Resource resource = new UrlResource(filePath.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            System.out.println("Resource doesn't exist or is not readable");
            throw new NotFoundException("Image file not found");
        }

        System.out.println("Resource found:" + resource);

        String contentType = Files.probeContentType(filePath);
        MediaType mediaType = (contentType != null) ? MediaType.parseMediaType(contentType) : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(resource);
    }

    // Needs also PUT endpoint to update avatar
    @PutMapping("/internal/avatar")
    public ResponseEntity<AvatarResponseDTO> updateAvatar(
            @Valid @ModelAttribute AvatarUpdateRequest dto
    ) throws IOException {

        AvatarResponseDTO response = mediaService.updateAvatar(dto.getNewAvatar(), dto.getOldAvatar());

        return ResponseEntity.ok(response);
    }

    // delete avatar from server
    @DeleteMapping("/internal/avatar/{path}")
    public ResponseEntity<?> deleteAvatar(
            @PathVariable String path
    ) {
        mediaService.deleteAvatar(path);
        return ResponseEntity.ok().build();
    }


}
