package com.buy01.media.service;

import com.buy01.media.MediaServiceApplication;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ProductEventConsumer {

    private final MediaService mediaService;

    @Value("${kafka.topic.product-deleted}")
    private String productDeletedTopic;

    public ProductEventConsumer(MediaService mediaService) {
        this.mediaService = mediaService;
    }

    @KafkaListener(topics = "${kafka.topic.product-deleted}", groupId = "product-service-group")
    public void handleProductDeleted(String productId) {

        // Call service to delete all products for this user
        mediaService.deleteMediaByProductId(productId);
    }
}
