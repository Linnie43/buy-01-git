package com.buy01.product.controller;

import com.buy01.product.model.Product;
import com.buy01.product.model.Role;
import com.buy01.product.security.AuthDetails;
import com.buy01.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.buy01.product.dto.ProductResponseDTO;
import com.buy01.product.security.SecurityUtils;
import com.buy01.product.dto.ProductUpdateRequest;
import com.buy01.product.dto.ProductCreateDTO;
import jakarta.validation.Valid;

@RestController // indicates that this class is a REST controller and handles HTTP requests
@RequestMapping("/api/products") // base URL for all endpoints in this controller
public class ProductController {

    private final ProductService productService;
    private final SecurityUtils securityUtils;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);


    public ProductController(ProductService productService, SecurityUtils securityUtils) {
        this.productService = productService;
        this.securityUtils = securityUtils;
    }

    // add new product, role seller or admin
    @PostMapping
    public ResponseEntity<?> createProduct(
            @RequestHeader("Authorization") String authHeader,
            @Valid @ModelAttribute ProductCreateDTO request) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);
        log.info("Creating product for user ID: {} with role: {}", currentUser.getCurrentUserId(), currentUser.getRole());

        if (currentUser.getRole().equals(Role.SELLER) || (currentUser.getRole().equals(Role.ADMIN) && request.getUserId() == null )) {
            request.setUserId(currentUser.getCurrentUserId());
        }

        ProductResponseDTO newProduct = productService.createProduct(request, currentUser);

        return ResponseEntity.ok(newProduct);
    }

    // get all products
    @GetMapping
    public List<ProductResponseDTO> getAllProducts(
            @RequestHeader(value = "Authorization", required = false) String authHeader
            ) {
        final AuthDetails currentUser = (authHeader != null) ? securityUtils.getAuthDetails(authHeader) : null;

        return productService.getAllProducts().stream()
                .map(p -> {
                    List<String> images = productService.getProductImageIds(p.getProductId());
                    return new ProductResponseDTO(
                            p.getProductId(),
                            p.getName(),
                            p.getDescription(),
                            p.getPrice(),
                            p.getQuantity(),
                            p.getUserId(),
                            images,
                            currentUser != null && currentUser.getCurrentUserId().equals(p.getUserId())
                    );
                })
                .toList();
    }


    // get a specific product by ID
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String productId) {

        AuthDetails currentUser = null;

        if (authHeader != null) {
            currentUser = securityUtils.getAuthDetails(authHeader);
        }

        Product p = productService.getProductById(productId);
        List<String> images = productService.getProductImageIds(p.getProductId());
        if (images == null) images = Collections.emptyList();

        ProductResponseDTO product = new ProductResponseDTO(
                p.getProductId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getQuantity(),
                p.getUserId(),
                images,
                currentUser.getCurrentUserId() != null && currentUser.getCurrentUserId().equals(p.getUserId())
        );

        return ResponseEntity.ok(product);
    }

    // get all products of the current logged-in user
    @GetMapping("/my-products")
    public List<ProductResponseDTO> getMyProducts(
            @RequestHeader("Authorization") String authHeader
    ) {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);
        return productService.getAllProductsByUserId(currentUser.getCurrentUserId(), currentUser);
    }

    // get all products of the current logged-in user
    @GetMapping("/internal/my-products/{userId}")
    public List<ProductResponseDTO> getUsersProducts(
            @PathVariable String userId
    ) {

        List<ProductResponseDTO> products = productService.getAllProductsByUserId(userId, new AuthDetails(userId, Role.SELLER));
        if  (products.isEmpty()) {
            log.info("No products for user ID: {}", userId);
            return new ArrayList<>();
        }

        return products;
    }

    // internal endpoint to get product by ID without validation (when adding to the cart for example)
    @GetMapping("/internal/{productId}")
    public ProductResponseDTO getProductInternal(
            @PathVariable String productId
    ) {

        Product product = productService.getProductById(productId);

        return new ProductResponseDTO(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getUserId(),
                null,
                false);
    }

    @PutMapping("/internal/quantity/{productId}")
    public ResponseEntity<Void> updateProductQuantityInternal(
            @PathVariable String productId,
            @RequestParam int quantityChange
    ) {
        productService.updateProductQuantity(productId, quantityChange);
        return ResponseEntity.ok().build();
    }


    // update a specific product by ID
    @PutMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String productId,
            @Valid @ModelAttribute ProductUpdateRequest request) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        ProductResponseDTO updated = productService.updateProduct(productId, request, currentUser);

        return ResponseEntity.ok(updated);
    }


    // delete a specific product by ID
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String productId
    ) {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        productService.deleteProduct(productId, currentUser);

        return ResponseEntity.ok().build();
    }
}
