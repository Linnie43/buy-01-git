package com.buy01.media.controller;

import com.buy01.media.dto.MediaCreateDTO;
import com.buy01.media.dto.MediaResponseDTO;
import com.buy01.media.exception.NotFoundException;
import com.buy01.media.security.SecurityUtils;
import com.buy01.media.model.Media;
import com.buy01.media.repository.MediaRepository;
import com.buy01.media.service.MediaService;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/media")
public class MediaController {

    private final MediaRepository mediaRepository;
    private final MediaService mediaService;
    private final SecurityUtils securityUtils;

    public MediaController(MediaRepository mediaRepository,MediaService mediaService,SecurityUtils securityUtils) {
        this.mediaRepository = mediaRepository;
        this.mediaService = mediaService;
        this.securityUtils = securityUtils;
    }

    // uploading media to the server, validating and saving metadata to database
    @PostMapping("/images")
    public ResponseEntity<List<MediaResponseDTO>> uploadImage(
            @RequestHeader("Authorization") String authHeader,
            @Valid @ModelAttribute MediaCreateDTO dto) {
        String currentUserId = securityUtils.getCurrentUserId(authHeader);
        String productId = dto.getProductId();

        // Stream files, save them and create a list of MediaResponseDTO for return
        List<MediaResponseDTO> result = dto.getFiles().stream()
                .map(file -> {
                    Media media = mediaService.saveImage(file, productId, currentUserId);
                    return new MediaResponseDTO(media.getId(), media.getPath(), media.getProductId());
                })
                .toList();

        return ResponseEntity.ok(result);
    }

    // serves the raw image bytes
    @GetMapping("/images/{id}")
    public ResponseEntity<Resource> getImage(@PathVariable String id) throws MalformedURLException {
        Media media = mediaRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Image not found"));

        Path filePath = Paths.get(media.getPath()).toAbsolutePath();
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic())
                .body(resource);
    }

    @DeleteMapping("images/{id}")
    public ResponseEntity<?> deleteImage(@PathVariable String id, Authentication auth) {
        String currentUserId = auth.getName(); // get logged-in user ID
        mediaService.deleteMedia(id, currentUserId);

        return ResponseEntity.ok().build();
    }

}
