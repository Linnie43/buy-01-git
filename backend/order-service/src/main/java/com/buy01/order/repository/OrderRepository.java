package com.buy01.order.repository;

import com.buy01.order.model.Order;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findAllOrdersByUserId(String userId);
}

