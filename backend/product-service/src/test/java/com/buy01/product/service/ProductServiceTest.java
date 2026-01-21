package com.buy01.product.service;

import com.buy01.product.client.MediaClient;
import com.buy01.product.client.UserClient;
import com.buy01.product.dto.ProductCreateDTO;
import com.buy01.product.dto.ProductResponseDTO;
import com.buy01.product.dto.ProductUpdateDTO;
import com.buy01.product.dto.ProductUpdateRequest;
import com.buy01.product.exception.ForbiddenException;
import com.buy01.product.exception.NotFoundException;
import com.buy01.product.model.Product;
import com.buy01.product.model.ProductCategory;
import com.buy01.product.model.Role;
import com.buy01.product.repository.ProductRepository;
import com.buy01.product.security.AuthDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaClient mediaClient;

    @Mock
    private UserClient userClient;

    @Mock
    private ProductEventService productEventService;

    @InjectMocks
    private ProductService productService;

    static class TestProduct extends Product {
        TestProduct(String productId, String name, String description, double price, int quantity,
                ProductCategory category, String userId) {
            super(productId, name, description, price, quantity, category, userId);
        }
    }

    private static final Product product1 = new TestProduct("prod-1", "Product 1", "Description 1", 10.0, 5, ProductCategory.OTHER, "user-1");
    private static final Product product2 = new TestProduct("prod-2", "Product 2", "Description 2", 20.0, 3, ProductCategory.OTHER, "user-2");

    @BeforeEach
    void setUp() {
        // productService is created by @InjectMocks with all mocks injected
    }

    @Test
    @DisplayName("createProduct with valid request returns ProductResponseDTO")
    void createProduct() throws IOException {

        ProductCreateDTO request = mock(ProductCreateDTO.class);
        when(request.getName()).thenReturn("Valid Name");
        when(request.getDescription()).thenReturn("A valid description");
        when(request.getPrice()).thenReturn(9.99);
        when(request.getQuantity()).thenReturn(5);
        when(request.getCategory()).thenReturn(ProductCategory.OTHER);
        when(request.getUserId()).thenReturn("current-user-1"); // Use the actual user ID
        when(request.getImagesList()).thenReturn(null);

        when(userClient.getRoleIfUserExists("current-user-1")).thenReturn(Role.SELLER); // Mock user role check

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return new TestProduct("prod-1", p.getName(), p.getDescription(), p.getPrice(), p.getQuantity(),
                    p.getCategory(), p.getUserId());
        });

        ProductResponseDTO resp = productService.createProduct(request, new AuthDetails("current-user-1", Role.SELLER));

        assertNotNull(resp);
        assertEquals("prod-1", resp.getProductId());
        assertEquals("Valid Name", resp.getName());
        assertEquals("current-user-1", resp.getUserId());
    }

    @Test
    @DisplayName("createProduct with forbidden role throws ForbiddenException")
    void createProductForbidden() throws IOException {
        ProductCreateDTO request = mock(ProductCreateDTO.class);
        assertThrows(ForbiddenException.class,
                () -> productService.createProduct(request, new AuthDetails("some-user", Role.CLIENT)));
        verify(productRepository, never()).save(any());
        verify(mediaClient, never()).postProductImages(anyString(), anyList());
        verify(userClient, never()).getRoleIfUserExists(anyString());
    }

    @Test
    @DisplayName("getProductById with non-existing ID throws NotFoundException")
    void getProductByIdNotFound() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductById("missing", null));
    }

    @Test
    @DisplayName("updateProduct by owner updates successfully and publishes event")
    void updateProduct() throws IOException {

        String productId = "prod-1";
        Product existing = new TestProduct(productId, "Old", "old desc", 5.0, 2, ProductCategory.OTHER, "owner-1");
        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        ProductUpdateRequest request = mock(ProductUpdateRequest.class);
        when(request.getName()).thenReturn("New Name");
        when(request.getDescription()).thenReturn("New desc");
        when(request.getPrice()).thenReturn(10.0);
        when(request.getQuantity()).thenReturn(3);
        when(request.getCategory()).thenReturn(ProductCategory.OTHER);
        when(request.getDeletedImageIds()).thenReturn(List.of());
        when(request.getImages()).thenReturn(List.of());

        when(mediaClient.updateProductImages(eq(productId), anyList(), anyList())).thenReturn(List.of());
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return new TestProduct(productId, p.getName(), p.getDescription(), p.getPrice(), p.getQuantity(),
                    p.getCategory(), p.getUserId());
        });

        ProductResponseDTO resp = productService.updateProduct(productId, request,
                new AuthDetails("owner-1", Role.SELLER));

        assertNotNull(resp);
        assertEquals("New Name", resp.getName());
        assertEquals(10.0, resp.getPrice());
        assertTrue(Boolean.TRUE.equals(resp.getIsProductOwner()));

        ArgumentCaptor<ProductUpdateDTO> captor = ArgumentCaptor.forClass(ProductUpdateDTO.class);

        verify(productEventService).publishProductUpdatedEvent(captor.capture());

        ProductUpdateDTO sent = captor.getValue();
        assertEquals(productId, sent.getProductId());
        assertEquals("New Name", sent.getName());
        assertEquals(10.0, sent.getPrice());
        assertEquals(3, sent.getQuantity());

    }

    @Test
    @DisplayName("deleteProduct by owner deletes successfully and publishes event")
    void deleteProduct() {
        String productId = "prod-1";
        Product existing = new TestProduct(productId, "Name", "desc", 1.0, 1, ProductCategory.OTHER, "owner-1");
        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        productService.deleteProduct(productId, new AuthDetails("owner-1", Role.SELLER));

        verify(productRepository).deleteById(productId);
        verify(productEventService).publishProductDeletedEvent(productId);
    }

    @Test
    @DisplayName("createProduct when mediaClient throws still creates product and returns empty media list")
    void createProductMediaError() throws IOException {
        ProductCreateDTO request = mock(ProductCreateDTO.class);
        when(request.getName()).thenReturn("Valid Name");
        when(request.getDescription()).thenReturn("Desc here");
        when(request.getPrice()).thenReturn(9.0);
        when(request.getQuantity()).thenReturn(1);
        when(request.getUserId()).thenReturn("current-user"); // Use the actual user ID

        var mockFile = new org.springframework.mock.web.MockMultipartFile(
                "file", "orig.jpg", "image/jpeg", new byte[] { 1 });
        when(request.getImagesList()).thenReturn(List.of(mockFile));

        when(userClient.getRoleIfUserExists("current-user")).thenReturn(Role.SELLER);

        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            return new TestProduct("prod-x", p.getName(), p.getDescription(), p.getPrice(), p.getQuantity(),
                    ProductCategory.OTHER, p.getUserId());
        });

        when(mediaClient.postProductImages(anyString(), anyList())).thenThrow(new RuntimeException("upstream fail"));

        ProductResponseDTO resp = productService.createProduct(request, new AuthDetails("current-user", Role.SELLER));
        assertNotNull(resp);
        assertEquals("prod-x", resp.getProductId());
        assertTrue(resp.getImages().isEmpty());
    }

    @Test
    @DisplayName("getProductImageIds returns empty list when mediaClient returns null")
    void getProductImageIdsNull() {
        when(mediaClient.getProductImageIds("p-1")).thenReturn(null);
        assertTrue(productService.getProductImageIds("p-1").isEmpty());
    }

    @Test
    @DisplayName("deleteProductsByUserId deletes each product and publishes events")
    void deleteProductsByUserId() {
        when(productRepository.findAllProductsByUserId("u1")).thenReturn(List.of(product1, product2));

        productService.deleteProductsByUserId("u1");

        verify(productRepository).delete(product1);
        verify(productRepository).delete(product2);
        verify(productEventService).publishProductDeletedEvent(product1.getProductId());
        verify(productEventService).publishProductDeletedEvent(product2.getProductId());
    }

    @Test
    @DisplayName("authProductOwner allows owner and admin, forbids others")
    void authProductOwner() {
        assertThrows(ForbiddenException.class, () -> productService.authProductOwner(product1, "someone-else", Role.SELLER));
    }

    @Test
    @DisplayName("createProduct with name too short throws IllegalArgumentException")
    void createProductBadRequest() {
        ProductCreateDTO request = mock(ProductCreateDTO.class);
        when(request.getName()).thenReturn("abc?!"); // only stub used by validation
        assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(request, new AuthDetails("current-user", Role.SELLER)));
    }

    @Test
    @DisplayName("getAllProducts returns sorted page of products")
    void getAllProducts() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        Page<Product> productPage = new PageImpl<>(List.of(product1), pageable, 1);
        when(productRepository.findAllByFilters("", null, null, null, pageable))
                .thenReturn(productPage);
        Page<ProductResponseDTO> all = productService.getAllProducts(null, null, null, null, pageable);
        assertEquals(1, all.getTotalPages());
        assertEquals(product1.getProductId(), all.getContent().get(0).getProductId());
    }

    @Test
    @DisplayName("updateProductStock updates stock and reserved quantities correctly")
    void updateProductStock() {
        String productId = "prod-1";
        Product existing = new TestProduct(productId, "Name", "desc", 5.0, 10, ProductCategory.OTHER, "owner-1");
        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        productService.updateProductQuantity(productId, -4);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertEquals(6, saved.getQuantity());
        assertEquals(4, saved.getReservedQuantity());
    }

    @Test
    @DisplayName("removeReserveQuantityForOrderPlaced updates stock and reserved quantities correctly")
    void removeReserveQuantityForOrderPlaced() {
        String productId = "prod-1";
        Product existing = new TestProduct(productId, "Name", "desc", 5.0, 10, ProductCategory.OTHER, "owner-1");
        existing.setReservedQuantity(5);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        productService.removeReserveQuantityForOrderPlaced(productId, -3);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertEquals(10, saved.getQuantity());
        assertEquals(2, saved.getReservedQuantity());
    }

    @Test
    @DisplayName("returnCancelledItemsToStock updates stock and reserved quantities correctly")
    void returnCancelledItemsToStock() {
        String productId = "prod-1";
        Product existing = new TestProduct(productId, "Name", "desc", 5.0, 8, ProductCategory.OTHER, "owner-1");
        existing.setReservedQuantity(4);
        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        productService.returnCancelledItemToStock(productId, 3);

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(captor.capture());
        Product saved = captor.getValue();
        assertEquals(11, saved.getQuantity());
        assertEquals(4, saved.getReservedQuantity());
    }

}
