package com.buy01.product.client;

import com.buy01.product.dto.MediaResponseDTO;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

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
        this.mediaServiceBaseUrl = "http://localhost:8082/api/media";
    }

    public List<String> getProductImageIds(String productId) {
        String url = mediaServiceBaseUrl + "/internal/images/productId/" + productId;
        System.out.println("Request url: " + url);

        ResponseEntity<List> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                null, // no headers needed for internal calls
                List.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to get images: " + response.getStatusCode());
        }

        return response.getBody();
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
            throw new RuntimeException("Failed to upload images: " + response.getStatusCode());
        }

        return mediaIds.stream()
                .map(MediaResponseDTO::getId)
                .collect(Collectors.toList());
    }

}
