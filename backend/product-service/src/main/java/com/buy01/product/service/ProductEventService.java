package com.buy01.product.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class ProductEventService {

    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.product-deleted}")
    private String productDeletedTopic;

    public void publishProductDeletedEvent(String productId) {
        kafkaTemplate.send(productDeletedTopic, productId);
    }
}
