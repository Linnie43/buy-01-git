package com.buy01.product.service;

import com.buy01.product.dto.ProductUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

    // Remove @InjectMocks to prevent auto-injection conflicts
    private ProductEventService productEventService;

    private final String DELETED_TOPIC = "test-deleted-topic";
    private final String UPDATED_TOPIC = "test-updated-topic";

    @BeforeEach
    void setUp() {
        // 1. Manually instantiate to ensure mocks are assigned correctly
        productEventService = new ProductEventService(stringKafkaTemplate, dtoKafkaTemplate);

        // 2. Set the @Value fields
        ReflectionTestUtils.setField(productEventService, "productDeletedTopic", DELETED_TOPIC);
        ReflectionTestUtils.setField(productEventService, "productUpdatedTopic", UPDATED_TOPIC);
    }

    @Test
    void publishProductDeletedEvent() {
        String productId = "prod-123";

        productEventService.publishProductDeletedEvent(productId);

        // Verify the interaction on the specific mock
        verify(stringKafkaTemplate).send(DELETED_TOPIC, productId);
    }
}