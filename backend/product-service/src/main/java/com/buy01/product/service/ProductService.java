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
import java.util.Collections;
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
        // validate userId
        validateUserId(request.getUserId());

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription().trim());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setUserId(request.getUserId());

        if (request.getImagesList() != null && request.getImagesList().size() > 5) {
            throw new BadRequestException("You can upload up to 5 images.");
        }

        Product savedProduct = productRepository.save(product);
        System.out.println("Saved product : " + savedProduct);

        List<String> mediaIds = List.of();

        if (request.getImagesList() != null) {
            System.out.println("Number of images uploaded: " + request.getImagesList().size());
            try {
                mediaIds = mediaClient.postProductImages(savedProduct.getProductId(), request.getImagesList());
                System.out.println("Uploaded product images: " + mediaIds);
            } catch (Exception e) {
                System.out.println("Failed to upload images: " + e.getMessage());
                // decide: either fail entire request or continue without images
            }
        }

        System.out.println("Uploaded product images: " + mediaIds);

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

    // Get all products by userId
    public List<ProductResponseDTO> getAllProductsByUserId(String userId, String role, String callerId) {
        List<Product> products;
        if (role.equals("ADMIN")) {
            // Admin can see any user's products
            products = productRepository.findAllProductsByUserId(userId);
        } else if (role.equals("SELLER")) {
            // Seller can only see their own products
            if (!userId.equals(callerId)) {
                throw new ForbiddenException("SELLER can only access their own products");
            }
            products = productRepository.findAllProductsByUserId(userId);
        } else {
            throw new ForbiddenException(role);
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
                            product.getUserId().equals(callerId)
                    );
                })
                .toList();
    }

    // Update product, only ADMIN or the owner of the product can update
    public ProductResponseDTO updateProduct(String productId, ProductUpdateRequest request, String userId, String role) throws IOException {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException(productId));

        authProductOwner(product, userId, role);

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


        Product updatedProduct = productRepository.save(product);

        // return the full, current list of images for the product (existing minus deleted + newly uploaded)
        return new ProductResponseDTO(
                updatedProduct.getProductId(),
                updatedProduct.getName(),
                updatedProduct.getDescription(),
                updatedProduct.getPrice(),
                updatedProduct.getQuantity(),
                updatedProduct.getUserId(),
                newMediaIds,
                updatedProduct.getUserId().equals(userId)
        );
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
        productDescription = productDescription.trim();

        if (productDescription == null || productDescription.length() < 1 || productDescription.length() > 500) {
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
        if (role == null || !role.equals("ADMIN") && !role.equals("SELLER")) {
            throw new BadRequestException();
        }
    }

    // Call for mediaClient to get all product image ids
    public List<String> getProductImageIds(String productId) {
        return Optional.ofNullable(mediaClient.getProductImageIds(productId)).orElse(List.of());
    }

    // Authenticates the product owner (or ADMIN), otherwise throws an error
    public void authProductOwner(Product product, String userId, String role) {
        if (!product.getUserId().equals(userId) && !role.equals("ADMIN")) {
            throw new ForbiddenException("Only admin or product owner can update product");
        }
    }
}
