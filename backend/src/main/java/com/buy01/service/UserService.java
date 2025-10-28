package com.buy01.service;

import com.buy01.model.Product;
import com.buy01.model.Role;
import com.buy01.model.User;
import com.buy01.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.buy01.security.SecurityUtils;
import com.buy01.dto.UserUpdateRequest;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private ProductService productService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User createUser(User user) {
        checkEmailUniqueness(user.getEmail(), user.getRole());
        prepareUserForSave(user);

        try {
            return userRepository.save(user);
        } catch (Exception e) {
            handleSaveException(e);
            return null;
            }
        }

        // Check if email is unique for that role
        private void checkEmailUniqueness(String email, Role role) {
            if (userRepository.findByEmail(email).isPresent()) {
                throw new IllegalArgumentException("User with this email already exists");
            }
        }

        // Prepare user object before saving - set default role and encode password
        private void prepareUserForSave(User user) {
            if (user.getRole() == null) {
                throw new IllegalArgumentException("Please select a role");
            }

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                throw new IllegalArgumentException("Password cannot be null or empty");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        // Handle exceptions during save operation to the database
        private void handleSaveException(Exception e) {
            if (e.getMessage().contains("duplicate key error")) { // MongoDB specific error message
                throw new IllegalArgumentException("User with this email already exists");
            }
            throw new RuntimeException(e); // if some other error occurs
    }


    @PreAuthorize("hasAuthority('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // method to get current logged-in user id
    public String getCurrentUserId() {
        return SecurityUtils.getCurrentUserId();
    }

    // method to find user by id, needs validation what information is sent if own profile
    public Optional<User> findById(String userId) { // optional means it may or may not contain a non-null value
        return userRepository.findById(userId);
    }

    // method to find user by id or throw exception if not found
    public User findByIdOrThrow(String userId) { // used for /me and for the sellerName
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // method to find user by email, used in authentication
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // method to update user, only updating own profile
    public User updateUser(String userId, UserUpdateRequest request) {
        if (!userId.equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("Forbidden - user can only modify their own profile");
        }

        // Fetch existing user from DB
        User existingUser = userRepository.findById(userId).orElseThrow();

        if (request.getName() != null) existingUser.setName(request.getName());
        if (request.getEmail() != null) existingUser.setEmail(request.getEmail());

        // Only update password if provided
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        return userRepository.save(existingUser);
    }

    public void deleteUser(String userId) {
        if (!userId.equals(SecurityUtils.getCurrentUserId())) {
            throw new RuntimeException("Forbidden - user can only modify their own profile");
        }
        // delete all user's products
        List<Product> allProducts = productService.getAllProductsByUserId(userId);
        for (Product product : allProducts) {
            productService.deleteProduct(product.getProductId());
        }
        userRepository.deleteById(userId);
    }
}

//service is responsible for business logic and data manipulation. It chooses how to handle data and interacts with the repository layer.
//it doesn't handle HTTP requests directly, that's the controller's job.
//Service can validate, filter, do calculations, and enforce business rules before passing data to/from the repository.
