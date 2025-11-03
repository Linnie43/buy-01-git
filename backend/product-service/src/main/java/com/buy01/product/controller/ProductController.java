package com.buy01.product.controller;

import com.buy01.product.model.Product;
import com.buy01.product.service.ProductService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.buy01.product.dto.ProductResponseDTO;
import com.buy01.product.security.SecurityUtils;
import com.buy01.product.dto.ProductUpdateRequest;
import com.buy01.product.dto.ProductCreateDTO;
import jakarta.validation.Valid;
import com.buy01.product.security.JwtUtil;

@RestController // indicates that this class is a REST controller and handles HTTP requests
@RequestMapping("/api/products") // base URL for all endpoints in this controller
public class ProductController {

    private final ProductService productService;
    private final JwtUtil jwtUtil;

    public ProductController(ProductService productService,  JwtUtil jwtUtil) {
        this.productService = productService;
        this.jwtUtil = jwtUtil;
    }

    // add new product, only sellers
    @PostMapping
    public ProductResponseDTO createProduct(@RequestHeader("Authorization") String authHeader,
                                            @Valid @RequestBody ProductCreateDTO request) {
        Product saved = productService.createProduct(request);
        String token = jwtUtil.getToken(authHeader);

//        List<String> images = productService.getProductImages(saved.getProductId());
        List<String> images = null;
        return new ProductResponseDTO(
                saved.getProductId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getQuantity(),
                saved.getUserId(),
                images,
                true
        );
    }

    // get all products
    @GetMapping
    public List<ProductResponseDTO> getAllProducts() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return productService.getAllProducts().stream()
                .map(p -> {
                    List<String> images = productService.getProductImages(p.getProductId());
                    return new ProductResponseDTO(
                            p.getProductId(),
                            p.getName(),
                            p.getDescription(),
                            p.getPrice(),
                            p.getQuantity(),
                            p.getUserId(),
                            images,
                            p.getUserId().equals(currentUserId)
                    );
                })
                .toList();
    }


    // get a specific product by ID
    @GetMapping("/{productId}")
    public ProductResponseDTO getProductById(@PathVariable String productId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Product p = productService.getProductById(productId);
        List<String> images = productService.getProductImages(p.getProductId());

        return new ProductResponseDTO(
                p.getProductId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getQuantity(),
                p.getUserId(),
                images,
                p.getUserId().equals(currentUserId)
        );
    }

    // get all products of the current logged-in user
    @GetMapping("/my-products")
    public List<ProductResponseDTO> getMyProducts() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return productService.getAllProducts().stream()
                .filter(p -> p.getUserId().equals(currentUserId))
                .map(p -> {
                    List<String> images = productService.getProductImages(p.getProductId());
                    return new ProductResponseDTO(
                            p.getProductId(),
                            p.getName(),
                            p.getDescription(),
                            p.getPrice(),
                            p.getQuantity(),
                            p.getUserId(),
                            images,
                            true
                    );
                })
                .toList();
    }

    // renew a specific product by ID
    @PutMapping("/{productId}")
    public Object updateProduct(@PathVariable String productId,
                                @RequestBody ProductUpdateRequest request) {
        Product updated = productService.updateProduct(productId, request);
        List<String> images = productService.getProductImages(updated.getProductId());

            return new ProductResponseDTO(
                    updated.getProductId(),
                    updated.getName(),
                    updated.getDescription(),
                    updated.getPrice(),
                    updated.getQuantity(),
                    updated.getUserId(),
                    images,
                    true
            );
    }


    // delete a specific product by ID
    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
    }
}
