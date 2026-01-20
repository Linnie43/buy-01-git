package com.buy01.media.client;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private static final String PRODUCT_SERVICE_BASE_URL = "http://product-service:8081/api/products";

    public boolean isOwner(String productId, String userId) {
        String url = PRODUCT_SERVICE_BASE_URL + "/internal/products/" + productId + "/owner/" + userId;
        return Boolean.TRUE.equals(restTemplate.getForObject(url, Boolean.class));
    }
}

