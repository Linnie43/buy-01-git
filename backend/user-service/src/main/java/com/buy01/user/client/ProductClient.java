package com.buy01.user.client;

import com.buy01.user.dto.ProductDTO;
import com.buy01.user.exception.NotFoundException;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ProductClient {

    private final RestTemplate restTemplate;
    private static final String PRODUCT_SERVICE_BASE_URL = "http://product-service:8081/api/products";

    public ProductClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }

    public List<ProductDTO> getUsersProducts(String userId) {
        try {
            String url = PRODUCT_SERVICE_BASE_URL + "/internal/my-products/" + userId;

            List<ProductDTO> products = restTemplate.getForObject(url, List.class);

            if (products == null || products.isEmpty()) {
                return List.of();
            } else {
                return products;
            }
        } catch (HttpClientErrorException e) {
            throw new NotFoundException("Products not found");
        }
    }

}
