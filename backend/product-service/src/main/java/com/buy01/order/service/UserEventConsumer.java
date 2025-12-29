package com.buy01.order.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class UserEventConsumer {

    private final ProductService productService;

    @Value("${kafka.topic.user-deleted}")
    private String userDeletedTopic;

    public UserEventConsumer(ProductService productService) {
        this.productService = productService;
    }

    @KafkaListener(topics = "${kafka.topic.user-deleted}", groupId = "product-service-group")
    public void handleUserDeleted(String userId) {
        System.out.println("Received user deleted event for userId: " + userId);

        // Call service to delete all products for this user
        productService.deleteProductsByUserId(userId);
    }
}
