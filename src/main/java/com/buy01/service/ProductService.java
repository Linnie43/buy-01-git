package com.buy01.service;

import com.buy01.model.Product;
import com.buy01.repository.ProductRepository;
import jakarta.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;

import static com.buy01.security.SecurityUtils.getCurrentUserId;
import static com.buy01.security.SecurityUtils.isAdmin;
import com.buy01.dto.ProductUpdateDTO;
import com.buy01.dto.ProductCreateDTO;
import com.buy01.security.SecurityUtils;


// service is responsible for business logic and data manipulation. It chooses how to handle data and interacts with the repository layer.
// it doesn't handle HTTP requests directly, that's the controller's job.
@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final UserService userService;

    @Autowired
    public ProductService(ProductRepository productRepository, UserService userService) {
        this.productRepository = productRepository;
        this.userService = userService;
    }

    // Create a new product, only USER and ADMIN can create products
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public Product createProduct(ProductCreateDTO request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        // adding current logged-in user
        product.setUserId(SecurityUtils.getCurrentUserId());
        product.setSellerName(userService.findByIdOrThrow(SecurityUtils.getCurrentUserId()).getName()); // lisa see


        return productRepository.save(product);
    }

    // Get all products, accessible by anyone (including unauthenticated users)
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @PermitAll
    public Product getProductById(String productId) {
        return findProductOrThrow(productId);
    }

    // Update product, only ADMIN or the owner of the product can update
    @PreAuthorize("hasAnyAuthority('ADMIN') or @productService.isOwner(#productId)")
    public Product updateProduct(String productId, ProductUpdateDTO request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Not found"));

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());

        return productRepository.save(product);
    }


    @PreAuthorize("hasAnyAuthority('ADMIN') or @productService.isOwner(#productId)")
    public void deleteProduct(String productId) {
        Product product = findProductOrThrow(productId);
        authorizeOwner(product);

        productRepository.deleteById(productId);
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

    // Find product by ID or throw exception if not found
    private Product findProductOrThrow(String productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
    }

    // Authorize that the current user is either the owner of the product or an admin
    private void authorizeOwner(Product product) {
        String currentUserId = getCurrentUserId();
        if (!product.getUserId().equals(currentUserId) && !isAdmin()) {
            throw new RuntimeException("Not authorized to perform this action");
        }
    }

    // Check if the current user is the owner of the product
    public boolean isOwner(String productId) {
        Product product = findProductOrThrow(productId);
        return product.getUserId().equals(getCurrentUserId());
    }
}
