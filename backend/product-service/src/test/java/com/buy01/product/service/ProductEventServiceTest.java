package com.buy01.product.service;

import com.buy01.product.dto.ProductUpdateDTO;
import com.buy01.product.model.ProductCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ProductEventServiceTest {

    @Mock
    private KafkaTemplate<String, String> stringKafkaTemplate;

    @Mock
    private KafkaTemplate<String, ProductUpdateDTO> dtoKafkaTemplate;

    @InjectMocks
    private ProductEventService productEventService;

    private final String DELETED_TOPIC = "test-deleted-topic";
    private final String UPDATED_TOPIC = "test-updated-topic";

    @BeforeEach
    void setUp() {
        // Since @Value isn't processed in MockitoExtension, we set them manually
        ReflectionTestUtils.setField(productEventService, "productDeletedTopic", DELETED_TOPIC);
        ReflectionTestUtils.setField(productEventService, "productUpdatedTopic", UPDATED_TOPIC);
    }

    @Test
    void publishProductDeletedEvent() {
        String productId = "prod-123";

        productEventService.publishProductDeletedEvent(productId);

        // Verify it sends the productId to the correct topic
        verify(stringKafkaTemplate).send(DELETED_TOPIC, productId);
    }

    @Test
    void publishProductUpdatedEvent() {
        ProductUpdateDTO dto = new ProductUpdateDTO(
                "prod-123", "New Name", 99.99, 10, ProductCategory.OTHER
        );

        productEventService.publishProductUpdatedEvent(dto);

        // Verify it sends the DTO to the correct topic
        verify(dtoKafkaTemplate).send(UPDATED_TOPIC, dto);
    }
}