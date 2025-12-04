package com.buy01.media.repository;

import com.buy01.media.model.Media;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface MediaRepository extends MongoRepository<Media, String> {
    List<Media> getMediaByProductId(String productId);

}
