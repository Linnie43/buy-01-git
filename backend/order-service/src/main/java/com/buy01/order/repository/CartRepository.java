package com.buy01.order.repository;

import com.buy01.order.model.Cart;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface CartRepository extends MongoRepository<Cart, String> {
    Cart findByUserId(String userId);
    List<Cart> findByItemsProductId(String productId);
    // Find ACTIVE carts updated before the specific date (fifteenMinsAgo)
    @Query("{ 'cartStatus': 'ACTIVE', 'createTime': { $lt: ?0 } }")
    List<Cart> findExpiredActiveCarts(Date expirationTime);

    // Find ABANDONED carts updated before the specific date (oneMinAgo)
    @Query("{ 'cartStatus': 'ABANDONED', 'updateTime': { $lt: ?0 } }")
    List<Cart> findExpiredAbandonedCarts(Date expirationTime);
}

