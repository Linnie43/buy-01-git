package com.buy01.product.service;

import com.buy01.product.dto.ProductUpdateDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductEventService {

    private final KafkaTemplate<String, String> stringKafkaTemplate;
    private final KafkaTemplate<String, ProductUpdateDTO> dtoKafkaTemplate;

    @Value("${kafka.topic.product-deleted}")
    private String productDeletedTopic;

    @Value("${kafka.topic.product-updated}")
    private String productUpdatedTopic;

    @Autowired
    public ProductEventService(
            KafkaTemplate<String, String> stringKafkaTemplate,
            KafkaTemplate<String, ProductUpdateDTO> dtoKafkaTemplate
    ) {
        this.stringKafkaTemplate = stringKafkaTemplate;
        this.dtoKafkaTemplate = dtoKafkaTemplate;
    }

    public void publishProductDeletedEvent(String productId) {
        stringKafkaTemplate.send(productDeletedTopic, productId);
    }

    public void publishProductUpdatedEvent(ProductUpdateDTO product) {
        dtoKafkaTemplate.send(productUpdatedTopic, product);
    }
}
