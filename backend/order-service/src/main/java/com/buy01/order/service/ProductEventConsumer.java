package com.buy01.order.service;

import com.buy01.order.dto.ProductUpdateDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;

public class ProductEventConsumer {

    private final CartService cartService;
    private static final Logger log = LoggerFactory.getLogger(ProductEventConsumer.class);


    @Value("${kafka.topic.product-updated}")
    private String productUpdatedTopic;

    public ProductEventConsumer(CartService cartService) {
        this.cartService = cartService;
    }

    @KafkaListener(topics = "${kafka.topic.product-updated}", groupId = "product-service-group")
    public void handleProductUpdated(ProductUpdateDTO product) {
        log.info("KAFKA LISTENER - Received product updated event for productId: {}", product.getProductId());

        cartService.updateCartProducts(product);
    }
}
