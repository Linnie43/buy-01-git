package com.buy01.product.client;

import com.buy01.product.dto.MediaResponseDTO;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MediaClient {

    private final RestTemplate restTemplate;
    private final String mediaServiceBaseUrl;

    public MediaClient() {
        this.restTemplate = new RestTemplate();
        // Local dev URL; will switch to container name in Docker
        this.mediaServiceBaseUrl = "http://media-service:8082/api/media";
    }

    public List<String> getProductImageIds(String productId) {
        String url = mediaServiceBaseUrl + "/internal/images/productId/" + productId;
        System.out.println("Request url: " + url);

        ResponseEntity<List<MediaResponseDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null, // no headers needed for internal calls
                new ParameterizedTypeReference<List<MediaResponseDTO>>() {}
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to get images: " + response.getStatusCode());
        }
        List<MediaResponseDTO> mediaResponses = response.getBody();
        if (mediaResponses == null) {
            return List.of();
        }

        return mediaResponses.stream()
                .map(MediaResponseDTO::getId)
                .collect(Collectors.toList());
    }

    public List<String> postProductImages(String productId, List<MultipartFile> images) throws IOException {
        String url = mediaServiceBaseUrl + "/internal/images";
        System.out.println("Request url: " + url);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("productId", productId);

        for (MultipartFile file : images) {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename(); // keep original filename
                }
            };
            body.add("files", resource);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        ResponseEntity<List<MediaResponseDTO>> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                requestEntity,
                new ParameterizedTypeReference<List<MediaResponseDTO>>() {}
        );

        List<MediaResponseDTO> mediaIds = response.getBody();
        System.out.println("Uploaded product images: " + mediaIds);

        if (!response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Failed to save images: " + response.getStatusCode());
            throw new FileUploadException("Failed to upload images: " + response.getStatusCode());
        }

        return mediaIds.stream()
                .map(MediaResponseDTO::getId)
                .collect(Collectors.toList());
    }

    // updates product images by deleting specified ones and adding new ones
    public List<String> updateProductImages(
            String productId,
            List<String> imagesToDelete,
            List<MultipartFile> newImages
    ) throws IOException {
        String url = mediaServiceBaseUrl + "/internal/images/productId/" + productId;
        System.out.println("Request url: " + url);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        for (String id : imagesToDelete) {
            body.add("imagesToDelete", id);
        }

        for (MultipartFile file : newImages) {
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename(); // keep original filename
                }
            };
            body.add("newImages", resource);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<List<MediaResponseDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.PUT,
                    requestEntity,
                    new ParameterizedTypeReference<List<MediaResponseDTO>>() {
                    }
            );

            List<MediaResponseDTO> mediaIds = response.getBody();
            return mediaIds.stream()
                    .map(MediaResponseDTO::getId)
                    .collect(Collectors.toList());
        } catch (HttpClientErrorException e) {
            System.out.println("Error updating product images: " + e.getStatusCode() + " - " + e.getResponseBodyAsString());
            throw new ResponseStatusException(e.getStatusCode(), e.getResponseBodyAsString(), e);
        }
    }

//    public void deleteImage(String imageId) {
//        String url = mediaServiceBaseUrl + "/internal/images/" + imageId;
//        System.out.println("Request url: " + url);
//
//        ResponseEntity<Void> response = restTemplate.exchange(
//                url,
//                HttpMethod.DELETE,
//                null, // no headers needed for internal calls
//                Void.class
//        );
//
//        if (!response.getStatusCode().is2xxSuccessful()) {
//            throw new RuntimeException("Failed to delete image: " + response.getStatusCode());
//        }
//
//        System.out.println("Deleted image with id: " + imageId);
//    }

}
