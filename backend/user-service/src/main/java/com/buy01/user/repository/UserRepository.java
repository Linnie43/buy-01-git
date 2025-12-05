package com.buy01.user.repository;

import com.buy01.user.model.Role;
import com.buy01.user.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;


public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email); //for authentication
    Optional<User> findByEmailAndRole(String email, Role role);
    Optional <User> findUserByUserId(String id);
}


