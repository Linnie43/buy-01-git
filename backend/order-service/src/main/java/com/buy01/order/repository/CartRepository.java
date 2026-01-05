package com.buy01.order.repository;

import com.buy01.order.dto.ItemDTO;
import com.buy01.order.model.Cart;
import com.buy01.order.model.Order;
import com.buy01.order.model.OrderItem;
import com.buy01.order.security.AuthDetails;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CartRepository extends MongoRepository<Cart, String> {
    Cart findByUserIdAndCartStatus(String userId, CartStatus cartStatus);
    List<Cart> findByProductId(String productId);
    OrderItem deleteItemByProductId(String productId);
}

