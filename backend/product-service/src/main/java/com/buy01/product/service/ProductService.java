package com.buy01.product.service;

import com.buy01.product.exception.ForbiddenException;
import com.buy01.product.model.Product;
import com.buy01.product.repository.ProductRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

import com.buy01.product.dto.ProductUpdateRequest;
import com.buy01.product.dto.ProductCreateDTO;
import org.springframework.web.client.RestTemplate;


// service is responsible for business logic and data manipulation. It chooses how to handle data and interacts with the repository layer.
// it doesn't handle HTTP requests directly, that's the controller's job.
@Service
public class ProductService {

    @Autowired
    private final ProductRepository productRepository;
    private final RestTemplate restTemplate;
    @Autowired
    private ProductEventService productEventService;

    @Autowired
    public ProductService(ProductRepository productRepository,  RestTemplate restTemplate) {
        this.productRepository = productRepository;
        this.restTemplate = restTemplate;
    }

    // Create a new product, only USER and ADMIN can create products
    public Product createProduct(ProductCreateDTO request) {
        // validate name
        validateProductName(request.getName());
        // validate description
        validateProductDescription(request.getDescription());
        // validate price
        validateProductPrice(request.getPrice());
        // validate quantity
        validateProductQuantity(request.getQuantity());

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());

        // adding current logged-in user
        product.setUserId(request.getUserId());

        return productRepository.save(product);
    }

    // Get all products, accessible by anyone (including unauthenticated users)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by id, public endpoint
    public Product getProductById(String productId) {
        return findProductOrThrow(productId);
    }

    // Currently limited to ADMIN
    public List<Product> getAllProductsByUserId(String userId) {
        // validate that admin

        return productRepository.findAllProductsByUserId(userId);
    }

    // Update product, only ADMIN or the owner of the product can update
    public Product updateProduct(String productId, ProductUpdateRequest request, String userId, boolean isAdmin) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        // check if the user has right to update the product (Admin or product owner)
        if (!isAdmin || !userId.equals(product.getUserId())) {
            throw new ForbiddenException("Only admin or product owner can update product");
        }
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            validateProductName(request.getName());
            product.setName(request.getName().trim());
        }
        if (request.getDescription() != null && !request.getDescription().trim().isEmpty()) {
            validateProductDescription(request.getDescription());
            product.setDescription(request.getDescription().trim());
        }

        if (request.getPrice() != null) {
            validateProductPrice(request.getPrice());
            product.setPrice(request.getPrice());
        }

        if (request.getQuantity() != null) {
            validateProductQuantity(request.getQuantity());
            product.setQuantity(request.getQuantity());
        }

        return productRepository.save(product);
    }


    // Deleting product, accessible only by ADMIN or product owner
    public void deleteProduct(String productId, String userId, boolean isAdmin) {
        Optional<Product> product = productRepository.findById(productId);

        // validate that user has role ADMIN or is the product owner
        if (!isAdmin || !userId.equals(product.get().getUserId())) {
            throw new ForbiddenException("Only admin or product owner can delete product");
        }

        productRepository.deleteById(productId);
        productEventService.publishProductDeletedEvent(productId);
    }

    // Delete all products from a specific user.
    // Called through kafka, consumer trusts that the action is already authorized and authenticated
    public void deleteProductsByUserId(String userId) {
        List<Product> products = productRepository.findAllProductsByUserId(userId);
        for (Product product : products) {
            String productId = product.getProductId();
            productRepository.delete(product);
            productEventService.publishProductDeletedEvent(productId); // publish the event of deleted productId
        }
    }

    // Helper methods

    // Validate product details
    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null) {
            throw new IllegalArgumentException("Price is required");
        }
        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
    }

    private void validateProductName(String productName) {
        if (productName == null || productName.isBlank()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (productName.length() < 5 || productName.length() > 255) {
            throw new IllegalArgumentException("Product name must be between 5 and 255 characters");
        }

        if (!productName.matches("^[A-Za-z0-9 ]+$")) {
            throw new IllegalArgumentException("Product name must contain only alphanumeric characters");
        }
    }

    private void validateProductDescription(String productDescription) {
        if (productDescription.length() > 500) {
            throw new IllegalArgumentException("Product description must be under 500 characters");
        }
    }

    private void validateProductPrice(Double productPrice) {
        if (productPrice == null || productPrice <= 0) {
            throw new IllegalArgumentException("Product price must be over 0");
        }
        if (productPrice > 100000) {
            throw new IllegalArgumentException("Product price must be under 100000");
        }
    }

    private void validateProductQuantity(Integer productQuantity) {
        if (productQuantity == null || productQuantity < 0) {
            throw new IllegalArgumentException("Product quantity can't be negative or empty");
        }
    }

    // Find product by ID or throw exception if not found
    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public List<String> getProductImages(String productId) {
        try {
            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            // GET image urls via gateway
            ResponseEntity<String> response = restTemplate.exchange(
                    "http://gateway:8443/media-service/api/media/productId/" + productId,
                    HttpMethod.GET,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Getting images for productId failed: " + response.getStatusCode());
            }

            // Deserialize JSON array into List<String>
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(response.getBody(), new TypeReference<List<String>>() {});

        } catch (Exception e) {
            throw new RuntimeException("Error getting product images", e);
        }
    }
}
