package com.buy01.product.controller;

import com.buy01.product.model.Product;
import com.buy01.product.security.JwtUtil;
import com.buy01.product.service.ProductService;
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
    private final JwtUtil jwtUtil;
    private final SecurityUtils securityUtils;

    public ProductController(ProductService productService,  JwtUtil jwtUtil, SecurityUtils securityUtils) {
        this.productService = productService;
        this.jwtUtil = jwtUtil;
        this.securityUtils = securityUtils;
    }

    // add new product, only sellers
    @PostMapping
    public ProductResponseDTO createProduct(
            @RequestHeader("Authorization") String authHeader,
            @Valid @ModelAttribute ProductCreateDTO request) throws IOException {
        System.out.println("header: " + authHeader);

        String currentUserId = securityUtils.getCurrentUserId(authHeader);
        String role = securityUtils.getRole(authHeader);
        System.out.println("Creating product for user ID: " + currentUserId + " with role: " + role);

        if (!role.equals("ADMIN") || request.getUserId().isEmpty()) {
            request.setUserId(currentUserId);
        }

       return productService.createProduct(request, role, currentUserId);
    }

    // get all products
    @GetMapping
    public List<ProductResponseDTO> getAllProducts(
            @RequestHeader(value = "Authorization", required = false) String authHeader
            ) {
        final String currentUserId = (authHeader != null) ? securityUtils.getCurrentUserId(authHeader) : null;

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
                            currentUserId != null && currentUserId.equals(p.getUserId())
                    );
                })
                .toList();
    }


    // get a specific product by ID
    @GetMapping("/{productId}")
    public ProductResponseDTO getProductById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String productId) {

        String currentUserId = null;

        if (authHeader != null) {
            currentUserId = securityUtils.getCurrentUserId(authHeader);
        }
        System.out.println("Current user id getting product: " + currentUserId);
        Product p = productService.getProductById(productId);
//        List<String> images = productService.getProductImages(p.getProductId());
        List<String> images = null;

        return new ProductResponseDTO(
                p.getProductId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getQuantity(),
                p.getUserId(),
                images,
                currentUserId != null && currentUserId.equals(p.getUserId())
        );
    }

    // get all products of the current logged-in user
    @GetMapping("/my-products")
    public List<ProductResponseDTO> getMyProducts(
            @RequestHeader("Authorization") String authHeader
    ) {
        String currentUserId = securityUtils.getCurrentUserId(authHeader);
        return productService.getAllProducts().stream()
                .filter(p -> p.getUserId().equals(currentUserId))
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
                            currentUserId.equals(p.getUserId())
                    );
                })
                .toList();
    }

    // get all products of the current logged-in user
    @GetMapping("/internal/my-products/{userId}")
    public List<ProductResponseDTO> getUsersProducts(
            @PathVariable String userId
    ) {
        System.out.println("Fetching products for user ID: " + userId);

        List<Product> products = productService.getAllProductsByUserId(userId, "SELLER");
        if  (products.isEmpty()) {
            System.out.println("No products for user ID: " + userId);
            return new ArrayList<>();
        }

        return products.stream()
                .map(p -> {
                    // Call the method, but return empty list for now
                    List<String> images = productService.getProductImageIds(p.getProductId());
                    if (images == null) images = Collections.emptyList();

                    return new ProductResponseDTO(
                            p.getProductId(),
                            p.getName(),
                            p.getDescription(),
                            p.getPrice(),
                            p.getQuantity(),
                            p.getUserId(),
                            images, // will be empty for now
                            userId.equals(p.getUserId())
                    );
                })
                .toList();
    }

    // renew a specific product by ID
    @PutMapping("/{productId}")
    public Object updateProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String productId,
            @Valid @ModelAttribute ProductUpdateRequest request) throws IOException {

        String currentUserId = securityUtils.getCurrentUserId(authHeader);
        String role = securityUtils.getRole(authHeader);

        ProductResponseDTO updated = productService.updateProduct(productId, request, currentUserId, role);

            return updated;
    }


    // delete a specific product by ID
    @DeleteMapping("/{productId}")
    public void deleteProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String productId
    ) {
        String currentUserId = securityUtils.getCurrentUserId(authHeader);
        String role = securityUtils.getRole(currentUserId);

        productService.deleteProduct(productId,  currentUserId, role);
    }
}
