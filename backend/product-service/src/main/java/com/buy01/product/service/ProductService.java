package com.buy01.product.service;

import com.buy01.product.client.MediaClient;
import com.buy01.product.client.UserClient;
import com.buy01.product.dto.ProductResponseDTO;
import com.buy01.product.exception.ForbiddenException;
import com.buy01.product.exception.NotFoundException;
import com.buy01.product.model.Product;
import com.buy01.product.repository.ProductRepository;
import jakarta.ws.rs.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import com.buy01.product.dto.ProductUpdateRequest;
import com.buy01.product.dto.ProductCreateDTO;


// Service layer is responsible for business logic, validation, verification and data manipulation.
// It chooses how to handle data and interacts with the repository layer.
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final MediaClient mediaClient;
    private final UserClient userClient;
    private final ProductEventService productEventService;

    @Autowired
    public ProductService(ProductRepository productRepository, MediaClient mediaClient, UserClient userClient, ProductEventService productEventService) {
        this.productRepository = productRepository;
        this.mediaClient = mediaClient;
        this.userClient = userClient;
        this.productEventService = productEventService;
    }

    // Create a new product, only SELLER and ADMIN can create products
    public ProductResponseDTO createProduct(ProductCreateDTO request, String role, String currentUserId) throws IOException {

        // validate that user can create products
        if (currentUserId.isEmpty() || (!role.equals("ADMIN") && !role.equals("SELLER"))) {
            System.out.println("Forbidden: User ID is empty or role is not allowed - Role: " + role);
            throw new ForbiddenException("Your current role cannot create a product.");
        }

        String productOwnerId = currentUserId;

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

        if (!request.getUserId().isEmpty()){
            validateUserId(request.getUserId());
            productOwnerId = request.getUserId();
        }
        product.setUserId(productOwnerId);

        Product savedProduct = productRepository.save(product);


        if (request.getImagesList() != null) {
            System.out.println("Number of images uploaded: " + request.getImagesList().size());
            if (request.getImagesList().size() > 5) {
                throw new BadRequestException("You can upload up to 5 images.");
            }
        }

        List<String> mediaIds = mediaClient.postProductImages(savedProduct.getProductId(), request.getImagesList());

        return new ProductResponseDTO(
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getQuantity(),
                savedProduct.getUserId(),
                mediaIds,
                savedProduct.getUserId().equals(currentUserId)
        );
    }

    // Get all products, accessible by anyone (including unauthenticated users)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get product by id, public endpoint
    public Product getProductById(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(productId));
    }

    // Get all products by userId, currently limited to ADMIN
    public List<Product> getAllProductsByUserId(String userId, String role) {

        if (!role.equals("ADMIN") && !role.equals("SELLER")) {
            throw new ForbiddenException(role);
        }

        return productRepository.findAllProductsByUserId(userId);
    }

    // Update product, only ADMIN or the owner of the product can update
    public Product updateProduct(String productId, ProductUpdateRequest request, String userId, String role) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(productId));

        authProductOwner(product, userId, role);

        if (request.getName() != null && !request.getName().trim().isEmpty()) { // if name exists, validate and update
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
    public void deleteProduct(String productId, String userId, String role) {
       Product product = productRepository.findById(productId)
               .orElseThrow(() -> new NotFoundException(productId));

        authProductOwner(product, userId, role);

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

    private void validateUserId(String userId) {
        // call UserClient to verify that the id exists.
        String role = userClient.getRoleIfUserExists(userId);
        if (!role.equals("ADMIN") && !role.equals("SELLER")) {
            throw new BadRequestException();
        }
    }

    // Call for mediaClient to get all product image ids
    public List<String> getProductImageIds(String productId) {
        return List.of();
//        return Optional.ofNullable(mediaClient.getProductImageIds(productId)).orElse(List.of());
    }

    // Authenticates the product owner (or ADMIN), otherwise throws an error
    public void authProductOwner(Product product, String userId, String role) {
        if (!product.getUserId().equals(userId) || !role.equals("ADMIN")) {
            throw new ForbiddenException("Only admin or product owner can update product");
        }
    }
}
