package com.buy01.order.repository;

import com.buy01.order.model.Product;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> getProductByName(String productName); //later for search purposes
    List<Product> findAllProductsByUserId(String userId);
}

