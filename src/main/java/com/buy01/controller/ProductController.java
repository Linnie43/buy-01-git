package com.buy01.controller;

import com.buy01.model.Product;
import com.buy01.service.ProductService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.buy01.dto.ProductResponse;
import com.buy01.service.UserService;
import com.buy01.security.SecurityUtils;
import com.buy01.dto.UpdateProductRequest;
import com.buy01.dto.CreateProductRequest;
import jakarta.validation.Valid;

@RestController // indicates that this class is a REST controller and handles HTTP requests
@RequestMapping("/products") // base URL for all endpoints in this controller
public class ProductController {

    private final ProductService productService;
    private final UserService userService;// service that does the business logic

    public ProductController(ProductService productService, UserService userService) {
        this.userService = userService;
        this.productService = productService;
    }

    // add new product
    @PostMapping
    public ProductResponse createProduct(@Valid @RequestBody CreateProductRequest request) {
        Product saved = productService.createProduct(request);
        String sellerName = userService.findByIdOrThrow(saved.getUserId()).getName();
        return new ProductResponse(
                saved.getProductId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPrice(),
                sellerName
        );
    }

    // get all products (if admin, return productId also)
    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts().stream()
                .map(p -> new ProductResponse(
                        p.getProductId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        userService.findByIdOrThrow(p.getUserId()).getName()
                ))
                .toList();
    }


    // get a specific product by ID
    @GetMapping("/{productId}")
    public ProductResponse getProductById(@PathVariable String productId) {
        Product p = productService.getProductById(productId);
        String sellerName = userService.findByIdOrThrow(p.getUserId()).getName();
        return new ProductResponse(
                p.getProductId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                sellerName);
    }

    // get all products of the current logged-in user
    @GetMapping("/my-products")
    public List<ProductResponse> getMyProducts() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return productService.getAllProducts().stream()
                .filter(p -> p.getUserId().equals(currentUserId))
                .map(p -> new ProductResponse(
                        p.getProductId(),
                        p.getName(),
                        p.getDescription(),
                        p.getPrice(),
                        userService.findByIdOrThrow(p.getUserId()).getName()
                ))
                .toList();
    }

    // renew a specific product by ID
    @PutMapping("/{productId}")
    public Object updateProduct(@PathVariable String productId,
                                @RequestBody UpdateProductRequest request) {
        Product updated = productService.updateProduct(productId, request);
        String sellerName = userService.findByIdOrThrow(updated.getUserId()).getName();

            return new ProductResponse(
                    updated.getProductId(),
                    updated.getName(),
                    updated.getDescription(),
                    updated.getPrice(),
                    sellerName
            );
    }


    // delete a specific product by ID
    @DeleteMapping("/{productId}")
    public void deleteProduct(@PathVariable String productId) {
        productService.deleteProduct(productId);
    }
}
