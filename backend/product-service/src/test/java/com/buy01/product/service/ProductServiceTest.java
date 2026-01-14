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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

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

    @BeforeEach
    void setUp() {
        // productService is created by @InjectMocks with all mocks injected
    }

    @Test
    void createProduct_validRequest_returnsProductResponseDTO() throws IOException {

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
        assertEquals("current-user-1", resp.getuserId());
    }

    @Test
    void createProduct_forbiddenRole_throwsForbiddenException() {
        ProductCreateDTO request = mock(ProductCreateDTO.class);
        assertThrows(ForbiddenException.class,
                () -> productService.createProduct(request, new AuthDetails("some-user", Role.CLIENT)));
    }

    @Test
    void getProductById_notFound_throwsNotFoundException() {
        when(productRepository.findById("missing")).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> productService.getProductById("missing", null));
    }

    @Test
    void updateProduct_ownerUpdates_success() throws IOException {

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
    void deleteProduct_ownerDeletes_callsRepositoryAndPublishesEvent() {
        String productId = "prod-1";
        Product existing = new TestProduct(productId, "Name", "desc", 1.0, 1, ProductCategory.OTHER, "owner-1");
        when(productRepository.findById(productId)).thenReturn(Optional.of(existing));

        productService.deleteProduct(productId, new AuthDetails("owner-1", Role.SELLER));

        verify(productRepository).deleteById(productId);
        verify(productEventService).publishProductDeletedEvent(productId);
    }

    @Test
    void createProduct_forbiddenRole_doesNotCallDownstreamClients() throws IOException {
        ProductCreateDTO request = mock(ProductCreateDTO.class);
        assertThrows(ForbiddenException.class,
                () -> productService.createProduct(request, new AuthDetails("some-user", Role.CLIENT)));
        verify(productRepository, never()).save(any());
        verify(mediaClient, never()).postProductImages(anyString(), anyList());
        verify(userClient, never()).getRoleIfUserExists(anyString());
    }

    @Test
    void createProduct_mediaClientThrows_stillCreatesProductAndReturnsEmptyMediaList() throws IOException {
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
    void getProductImageIds_returnsEmptyListWhenClientReturnsNull() {
        when(mediaClient.getProductImageIds("p-1")).thenReturn(null);
        assertTrue(productService.getProductImageIds("p-1").isEmpty());
    }

    @Test
    void deleteProductsByUserId_deletesEachAndPublishesEvent() {
        TestProduct p1 = new TestProduct("p1", "n", "d", 1.0, 1, ProductCategory.OTHER, "u1");
        TestProduct p2 = new TestProduct("p2", "n2", "d2", 2.0, 2, ProductCategory.OTHER, "u1");
        when(productRepository.findAllProductsByUserId("u1")).thenReturn(List.of(p1, p2));

        productService.deleteProductsByUserId("u1");

        verify(productRepository).delete(p1);
        verify(productRepository).delete(p2);
        verify(productEventService).publishProductDeletedEvent("p1");
        verify(productEventService).publishProductDeletedEvent("p2");
    }

    @Test
    void authProductOwner_nonOwnerNonAdmin_throwsForbiddenException() {
        TestProduct p = new TestProduct("p1", "n", "d", 1.0, 1, ProductCategory.OTHER, "owner-1");
        assertThrows(ForbiddenException.class, () -> productService.authProductOwner(p, "someone-else", Role.SELLER));
    }

    @Test
    void createProduct_nameTooShort_throwsIllegalArgumentException() {
        ProductCreateDTO request = mock(ProductCreateDTO.class);
        when(request.getName()).thenReturn("abc"); // only stub used by validation
        assertThrows(IllegalArgumentException.class,
                () -> productService.createProduct(request, new AuthDetails("current-user", Role.SELLER)));
    }

    @Test
    void getAllProducts_returnsSortedPage() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("createdAt").descending());
        TestProduct p = new TestProduct("p1", "n", "d", 1.0, 1, ProductCategory.OTHER, "u1");
        Page<Product> productPage = new PageImpl<>(List.of(p), pageable, 1);
        when(productRepository.findAllByFilters("", null, null, null, pageable))
                .thenReturn(productPage);
        Page<ProductResponseDTO> all = productService.getAllProducts(null, null, null, null, pageable);
        assertEquals(1, all.getTotalPages());
        assertEquals("p1", all.getContent().get(0).getProductId());
    }

}
