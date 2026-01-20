package com.buy01.order.service;

import com.buy01.order.dto.ProductUpdateDTO;
import com.buy01.order.model.ProductCategory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductEventConsumerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private ProductEventConsumer productEventConsumer;

    @Test
    @DisplayName("Should call cartService when product updated event is received")
    void handleProductUpdated() {
        // 1. Arrange: Create a mock DTO representing the Kafka message payload
        ProductUpdateDTO productUpdate = new ProductUpdateDTO(
                "prod-123",
                "Updated Name",
                25.0,
                10,
                ProductCategory.OTHER,
                "seller1"
        );

        // 2. Act: Manually call the listener method as if Kafka triggered it
        productEventConsumer.handleProductUpdated(productUpdate);

        // 3. Assert: Verify the consumer passed the DTO to the cartService
        verify(cartService).updateCartProducts(productUpdate);
    }
}