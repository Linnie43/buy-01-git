package com.buy01.product.repository;

import com.buy01.product.dto.ProductResponseDTO;
import com.buy01.product.model.Product;
import com.buy01.product.model.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface ProductRepository extends MongoRepository<Product, String> {
    List<Product> getProductByName(String productName); //later for search purposes
    List<Product> findAllProductsByUserId(String userId);
    int getQuantityByProductId(String productId);
    @Query("{ $and: [ " +
            "{ 'name': { $regex: ?0, $options: 'i' } }, " +
            "{ $or: [ { $expr: { $eq: [?1, null] } } , { 'price': { $gte: ?1 } } ] }, " +
            "{ $or: [ { $expr: { $eq: [?2, null] } } , { 'price': { $lte: ?2 } } ] } " +
            "{ $or: [ { $expr: { $eq: [?3, null] } } , { 'category': ?3 } } ] } " +
            "] }")
    Page<Product> findAllByFilters(String name, Double minPrice, Double maxPrice, ProductCategory category, Pageable pageable);
}

