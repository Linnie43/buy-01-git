package com.buy01.order.repository;

import com.buy01.order.dto.ItemDTO;
import com.buy01.order.model.Order;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface OrderRepository extends MongoRepository<Order, String> {
    List<Order> findOrdersByUserId(String userId);
    List<Order> findByItemsSellerId(String sellerId);
    @Aggregation(pipeline = {
            // 1. Optimization: Only pick orders that have this seller involved at all
            "{ '$match': { 'items.sellerId': ?0 } }",

            // 2. Explode the array: 1 Order with 5 items becomes 5 documents
            "{ '$unwind': '$items' }",

            // 3. CRITICAL STEP: Filter out items that belong to OTHER sellers
            "{ '$match': { 'items.sellerId': ?0 } }",

            // 4. Group by Product ID
            "{ '$group': { " +
                    "'_id': '$items.productId', " +
                    "'productName': { '$first': '$items.productName' }, " +
                    "'sellerId': { '$first': '$items.sellerId' }, " +
                    "'price': { '$first': '$items.price' }, " +
                    "'quantity': { '$sum': '$items.quantity' }, " +
                    "'subtotal': { '$sum': { '$multiply': ['$items.quantity', '$items.price'] } }" +
                    "} }",

            // 5. Sort by quantity Descending
            "{ '$sort': { 'quantity': -1 } }",

            // 6. Limit (Argument 1)
            "{ '$limit': ?1 }",

            // 7. Project to DTO
            "{ '$project': { " +
                    "'productId': '$_id', " +
                    "'name': '$productName', " +
                    "'sellerId': 1, 'price': 1, 'quantity': 1, 'subtotal': 1, '_id': 0 " +
                    "} }"
    })
    List<ItemDTO> findTopItemsBySellerId(String sellerId, int limit);
    @Aggregation(pipeline = {
            // 1. Filter by userId (Argument 0)
            "{ '$match': { 'userId': ?0 } }",

            // 2. Flatten the items list
            "{ '$unwind': '$items' }",

            // 3. Group by Product ID
            "{ '$group': { " +
                    "'_id': '$items.productId', " +
                    "'productName': { '$first': '$items.productName' }, " +
                    "'sellerId': { '$first': '$items.sellerId' }, " +
                    "'price': { '$first': '$items.price' }, " +
                    "'quantity': { '$sum': '$items.quantity' }, " +
                    "'subtotal': { '$sum': { '$multiply': ['$items.quantity', '$items.price'] } }" +
                    "} }",

            // 4. Sort by quantity Descending
            "{ '$sort': { 'quantity': -1 } }",

            // 5. Limit results (Argument 1)
            "{ '$limit': ?1 }",

            // 6. Project/Rename fields to match your DTO
            "{ '$project': { " +
                    "'productId': '$_id', " +
                    "'name': '$productName', " +
                    "'sellerId': 1, 'price': 1, 'quantity': 1, 'subtotal': 1, '_id': 0 " +
                    "} }"
    })
    List<ItemDTO> findTopItemsByUserId(String userId, int limit);

}
