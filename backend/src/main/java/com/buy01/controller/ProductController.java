package com.buy01.controller;

import com.buy01.model.Media;
import com.buy01.model.Product;
import com.buy01.repository.MediaRepository;
import com.buy01.service.MediaService;
import com.buy01.service.ProductService;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.buy01.dto.ProductResponseDTO;
import com.buy01.service.UserService;
import com.buy01.security.SecurityUtils;
import com.buy01.dto.ProductUpdateRequest;
import com.buy01.dto.ProductCreateDTO;
import jakarta.validation.Valid;

@RestController // indicates that this class is a REST controller and handles HTTP requests
@RequestMapping("/products") // base URL for all endpoints in this controller
public class ProductController {

    private final ProductService productService;
    private final UserService userService;// service that does the business logic
    private final MediaRepository mediaRepository;

    public ProductController(ProductService productService, UserService userService,  MediaRepository mediaRepository) {
        this.userService = userService;
        this.productService = productService;
        this.mediaRepository = mediaRepository;
    }

    // add new product, only sellers
    @PostMapping
    public ProductResponseDTO createProduct(@Valid @RequestBody ProductCreateDTO request) {
        Product saved = productService.createProduct(request);
        String sellerName = userService.findByIdOrThrow(saved.getUserId()).getName();
        List <Media> images = mediaRepository.getMediaByProductId(saved.getProductId());
        return new ProductResponseDTO(
                saved.getProductId(),
                saved.getName(),
                saved.getDescription(),
                saved.getPrice(),
                saved.getQuantity(),
                sellerName,
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
                    String ownerId = userService.findByIdOrThrow(p.getUserId()).getId();
                    List<Media> images = mediaRepository.getMediaByProductId(p.getProductId());
                    return new ProductResponseDTO(
                            p.getProductId(),
                            p.getName(),
                            p.getDescription(),
                            p.getPrice(),
                            p.getQuantity(),
                            ownerId,
                            images,
                            ownerId.equals(currentUserId)
                    );
                })
                .toList();
    }


    // get a specific product by ID
    @GetMapping("/{productId}")
    public ProductResponseDTO getProductById(@PathVariable String productId) {
        String currentUserId = SecurityUtils.getCurrentUserId();
        Product p = productService.getProductById(productId);
        String ownerId = userService.findByIdOrThrow(p.getUserId()).getId();
        List<Media> images = mediaRepository.getMediaByProductId(p.getProductId());

        return new ProductResponseDTO(
                p.getProductId(),
                p.getName(),
                p.getDescription(),
                p.getPrice(),
                p.getQuantity(),
                ownerId,
                images,
                ownerId.equals(currentUserId)
        );
    }

    // get all products of the current logged-in user
    @GetMapping("/my-products")
    public List<ProductResponseDTO> getMyProducts() {
        String currentUserId = SecurityUtils.getCurrentUserId();
        return productService.getAllProducts().stream()
                .filter(p -> p.getUserId().equals(currentUserId))
                .map(p -> {
                    String sellerName = userService.findByIdOrThrow(p.getUserId()).getName();
                    List<Media> images = mediaRepository.getMediaByProductId(p.getProductId());
                    return new ProductResponseDTO(
                            p.getProductId(),
                            p.getName(),
                            p.getDescription(),
                            p.getPrice(),
                            p.getQuantity(),
                            sellerName,
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
        String sellerName = userService.findByIdOrThrow(updated.getUserId()).getName();
        List<Media> images = mediaRepository.getMediaByProductId(updated.getProductId());

            return new ProductResponseDTO(
                    updated.getProductId(),
                    updated.getName(),
                    updated.getDescription(),
                    updated.getPrice(),
                    updated.getQuantity(),
                    sellerName,
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
