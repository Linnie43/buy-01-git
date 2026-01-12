package com.buy01.order.client;

import com.buy01.order.dto.ProductUpdateDTO;
import com.buy01.order.exception.ConflictException;
import com.buy01.order.exception.NotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class ProductClient {

    private final RestTemplate restTemplate;
    private final String productServiceBaseUrl;
    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);


    public ProductClient(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
        this.productServiceBaseUrl = "http://product-service:8081/api/products";
    }

    public ProductUpdateDTO getProductById(String productId) {
        try {
            String url = productServiceBaseUrl + "/internal/" + productId;
            log.info("Get product by id {}", url);
            ProductUpdateDTO product = restTemplate.getForObject(url, ProductUpdateDTO.class);
            log.info("Product from product service: Id {}, name {}, price {}, quantity {}, sellerId {}",
                    product.getProductId(), product.getProductName(), product.getProductPrice(), product.getQuantity(), product.getSellerId());
            return product;
        } catch (HttpClientErrorException e) {
            throw new NotFoundException("Product not found with ID: " + productId);
        }
    }

    public void updateQuantity(String productId, int quantity) {
        try {
            String url = productServiceBaseUrl + "/internal/quantity/" + productId;
            restTemplate.put(url, quantity);
        } catch (HttpClientErrorException e) {

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Product not found with ID: " + productId);
            }

            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                log.info("Conflict when updating quantity for productId: {}", productId);
                throw new ConflictException("Requested quantity is not available");
            }

            throw e;
        }
    }

    public void placeOrder(String productId, int quantity) {
        try {
            String url = productServiceBaseUrl + "/internal/order/" + productId;
            restTemplate.put(url, quantity);
        } catch (HttpClientErrorException e) {

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new NotFoundException("Product not found with ID: " + productId);
            }

            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                log.info("Conflict when placing order for productId: {}", productId);
                throw new ConflictException("Requested quantity is not available");
            }

            throw e;
        }
    }
}
