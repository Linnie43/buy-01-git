package com.buy01.product.service;

import com.buy01.product.client.MediaClient;
import com.buy01.product.client.UserClient;
import com.buy01.product.dto.ProductResponseDTO;
import com.buy01.product.dto.ProductUpdateDTO;
import com.buy01.product.exception.ConflictException;
import com.buy01.product.exception.ForbiddenException;
import com.buy01.product.exception.NotFoundException;
import com.buy01.product.model.Product;
import com.buy01.product.model.Role;
import com.buy01.product.repository.ProductRepository;
import com.buy01.product.security.AuthDetails;
import jakarta.ws.rs.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
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
    private static final Logger log = LoggerFactory.getLogger(ProductService.class);


    @Autowired
    public ProductService(ProductRepository productRepository, MediaClient mediaClient, UserClient userClient, ProductEventService productEventService) {
        this.productRepository = productRepository;
        this.mediaClient = mediaClient;
        this.userClient = userClient;
        this.productEventService = productEventService;
    }

    // Create a new product, only SELLER and ADMIN can create products
    public ProductResponseDTO createProduct(ProductCreateDTO request, AuthDetails currentUser) throws IOException {

        // validate that user can create products
        if (currentUser.getCurrentUserId().isEmpty() || (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getRole().equals(Role.SELLER))) {
            log.info("Forbidden: User ID is empty or role is not allowed - Role: {}", currentUser.getRole());
            throw new ForbiddenException("Your current role cannot create a product.");
        }

        // validate name
        validateProductName(request.getName());
        // validate description
        validateProductDescription(request.getDescription());
        // validate price
        validateProductPrice(request.getPrice());
        // validate quantity
        validateProductQuantity(request.getQuantity());
        // validate userId
        validateUserId(request.getUserId());

        Product product = new Product(request.getName().trim(), request.getDescription().trim(),
                request.getPrice(), request.getQuantity(), request.getUserId());

        if (request.getImagesList() != null && request.getImagesList().size() > 5) {
            throw new BadRequestException("You can upload up to 5 images.");
        }

        Product savedProduct = productRepository.save(product);

        List<String> mediaIds = List.of();

        if (request.getImagesList() != null) {
            log.info("Number of images uploaded: {}", request.getImagesList().size());
            try {
                mediaIds = mediaClient.postProductImages(savedProduct.getProductId(), request.getImagesList());
                log.info("Uploaded product images: {}", mediaIds);
            } catch (Exception e) {
                log.info("Failed to upload images: {}", e.getMessage());
                // continue without images
            }
        }

        log.info("Uploaded product images: {}", mediaIds);

        return new ProductResponseDTO(
                savedProduct.getProductId(),
                savedProduct.getName(),
                savedProduct.getDescription(),
                savedProduct.getPrice(),
                savedProduct.getQuantity(),
                savedProduct.getUserId(),
                mediaIds,
                savedProduct.getUserId().equals(currentUser.getCurrentUserId())
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

    // Get all products by userId
    public List<ProductResponseDTO> getAllProductsByUserId(String userId, AuthDetails currentUser) {
        List<Product> products;
        if (currentUser.getRole().equals(Role.ADMIN)) {
            // Admin can see any user's products
            products = productRepository.findAllProductsByUserId(userId);
        } else if (currentUser.getRole().equals(Role.SELLER)) {
            // Seller can only see their own products
            if (!userId.equals(currentUser.getCurrentUserId())) {
                throw new ForbiddenException("SELLER can only access their own products");
            }
            products = productRepository.findAllProductsByUserId(userId);
        } else {
            throw new ForbiddenException("This role cannot access the products by userId: " + currentUser.getRole());
        }

        return products.stream()
                .map(product -> {
                    // fetch images for each product
                    List<String> images = getProductImageIds(product.getProductId());
                    if (images == null) images = Collections.emptyList();

                    return new ProductResponseDTO(
                            product.getProductId(),
                            product.getName(),
                            product.getDescription(),
                            product.getPrice(),
                            product.getQuantity(),
                            product.getUserId(),
                            images, // now includes images
                            product.getUserId().equals(currentUser.getCurrentUserId())
                    );
                })
                .toList();
    }

    // Update product, only ADMIN or the owner of the product can update
    public ProductResponseDTO updateProduct(String productId, ProductUpdateRequest request, AuthDetails currentUser) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(productId));

        authProductOwner(product, currentUser.getCurrentUserId(), currentUser.getRole());

        //VALIDATE FIRST THEN SET
        validateProductName(request.getName());
        validateProductDescription(request.getDescription());
        validateProductPrice(request.getPrice());
        validateProductQuantity(request.getQuantity());

        // VALIDATE AND HANDLE IMAGES

        if (request.getDeletedImageIds() == null) {
            request.setDeletedImageIds(List.of());
        }
        if (request.getImages() == null) {
            request.setImages(List.of());
        }

        List<String> newMediaIds = mediaClient.updateProductImages(
                productId,
                request.getDeletedImageIds(),
                request.getImages()
        );

        // SET IF VALIDATED AND IMAGES HANDLED

        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setUpdateTime(new Date());


        Product updatedProduct = productRepository.save(product);

        // send productUpdate via Kafka
        productEventService.publishProductUpdatedEvent(new ProductUpdateDTO(
                updatedProduct.getProductId(),
                updatedProduct.getName(),
                updatedProduct.getDescription(),
                updatedProduct.getPrice(),
                updatedProduct.getQuantity()
        ));

        // return the full, current list of images for the product (existing minus deleted + newly uploaded)
        return new ProductResponseDTO(
                updatedProduct.getProductId(),
                updatedProduct.getName(),
                updatedProduct.getDescription(),
                updatedProduct.getPrice(),
                updatedProduct.getQuantity(),
                updatedProduct.getUserId(),
                newMediaIds,
                updatedProduct.getUserId().equals(currentUser.getCurrentUserId())
        );
    }

    // Update product quantity, called when product quantity is changed in the cart
    public void updateProductQuantity(String productId, int requestedQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(productId));
        int newQuantity = product.getQuantity() + requestedQuantity;
        if (newQuantity < 0) {
            throw new ConflictException("Insufficient product quantity, remaining quantity" + product.getQuantity());
        }
        product.setQuantity(newQuantity);
        product.setUpdateTime(new Date());
        productRepository.save(product);

        // send productUpdate via Kafka
        productEventService.publishProductUpdatedEvent(new ProductUpdateDTO(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity()
        ));
    }

    // Deleting product, accessible only by ADMIN or product owner
    public void deleteProduct(String productId, AuthDetails currentUser) {
       Product product = productRepository.findById(productId)
               .orElseThrow(() -> new NotFoundException(productId));

        authProductOwner(product, currentUser.getCurrentUserId(), currentUser.getRole());

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
        productDescription = productDescription.trim();

        if (productDescription == null || productDescription.isEmpty() || productDescription.length() > 500) {
            throw new IllegalArgumentException("Product description must be between 1 - 500 characters");
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
        if (role == null || !role.equals(Role.ADMIN) && !role.equals(Role.SELLER)) {
            throw new BadRequestException();
        }
    }

    // Call for mediaClient to get all product image ids
    public List<String> getProductImageIds(String productId) {
        return Optional.ofNullable(mediaClient.getProductImageIds(productId)).orElse(List.of());
    }

    // Authenticates the product owner (or ADMIN), otherwise throws an error
    public void authProductOwner(Product product, String userId, Role role) {
        if (!product.getUserId().equals(userId) && !role.equals(Role.ADMIN)) {
            throw new ForbiddenException("Only admin or product owner can update product");
        }
    }
}
