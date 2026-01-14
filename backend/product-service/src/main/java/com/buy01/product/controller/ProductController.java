package com.buy01.product.controller;

import com.buy01.product.exception.NotFoundException;
import com.buy01.product.model.Product;
import com.buy01.product.model.ProductCategory;
import com.buy01.product.model.Role;
import com.buy01.product.repository.ProductRepository;
import com.buy01.product.security.AuthDetails;
import com.buy01.product.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Pageable;

import java.io.IOException;
import java.util.ArrayList;
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
    private final ProductRepository productRepository;
    private final SecurityUtils securityUtils;
    private static final Logger log = LoggerFactory.getLogger(ProductController.class);


    public ProductController(ProductService productService, ProductRepository productRepository, SecurityUtils securityUtils) {
        this.productService = productService;
        this.productRepository = productRepository;
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
    public ResponseEntity<Page<ProductResponseDTO>> getAllProducts(
            @RequestParam(value = "search", required = false) String keyword,
            @RequestParam(value = "minPrice", required = false) Double minPrice,
            @RequestParam(value = "maxPrice", required = false) Double maxPrice,
            @RequestParam(value = "category", required = false) ProductCategory category,
            @PageableDefault(size = 10, sort = "createTime", direction = Sort.Direction.DESC) Pageable pageable
            ) {

        return ResponseEntity.ok(productService.getAllProducts(keyword, minPrice, maxPrice, category, pageable));
    }


    // get a specific product by ID
    @GetMapping("/{productId}")
    public ResponseEntity<?> getProductById(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @PathVariable String productId) {

        final AuthDetails currentUser = (authHeader != null) ? securityUtils.getAuthDetails(authHeader) : null;

        return ResponseEntity.ok(productService.getProductById(productId, currentUser));
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

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new NotFoundException("Product not found with ID: " + productId));

        return new ProductResponseDTO(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getQuantity(),
                product.getCategory(),
                product.getUserId(),
                null,
                false);
    }

    @PutMapping("/internal/quantity/{productId}")
    public ResponseEntity<Void> updateProductQuantityInternal(
            @PathVariable String productId,
            @RequestBody int quantityChange
    ) {
        log.info("Update product quantity: {} {}", productId, quantityChange);
        productService.updateProductQuantity(productId, quantityChange);
        return ResponseEntity.ok().build();
    }

    @PutMapping("internal/order/{productId}")
    public ResponseEntity<Void> updateReserveQuantityInternal(
            @PathVariable String productId,
            @RequestBody int reserveChange
    ) {
        log.info("Update product reserved quantity: {} {}", productId, reserveChange);
        productService.removeReserveQuantityForOrderPlaced(productId, reserveChange);
        return ResponseEntity.ok().build();
    }

    @PutMapping("internal/cancel/{productId}")
    public ResponseEntity<Void> returnCancelledItemToStock(
            @PathVariable String productId,
            @RequestBody int quantityToReturn
    ) {
        log.info("Return cancelled order quantity to stock: {} {}", productId, quantityToReturn);
        productService.returnCancelledItemToStock(productId, quantityToReturn);
        return ResponseEntity.ok().build();
    }


    // update a specific product by ID
    @PutMapping("/{productId}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String productId,
            @Valid @ModelAttribute ProductUpdateRequest request) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        return ResponseEntity.ok(productService.updateProduct(productId, request, currentUser));
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
