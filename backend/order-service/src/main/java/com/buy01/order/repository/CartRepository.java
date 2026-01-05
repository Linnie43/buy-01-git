package com.buy01.order.repository;

import com.buy01.order.model.Cart;
import com.buy01.order.model.OrderItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CartRepository extends MongoRepository<Cart, String> {
    Cart findByUserId(String userId);
    List<Cart> findByProductId(String productId);
    OrderItem deleteItemByProductId(String productId);
}

