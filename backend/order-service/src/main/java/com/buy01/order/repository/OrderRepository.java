package com.buy01.order.repository;

import com.buy01.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends MongoRepository<Order, String> {
    Optional<Order> getOrderById(String orderId);
    List<Order> findOrdersByUserId(String userId);
    List<Order> findByItemsSellerId(String sellerId);
}

