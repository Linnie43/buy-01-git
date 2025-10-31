package com.buy01.service;

import com.buy01.dto.UserUpdateRequest;
import com.buy01.exception.ForbiddenException;
import com.buy01.model.Product;
import com.buy01.model.Role;
import com.buy01.model.User;
import com.buy01.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.buy01.security.SecurityUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    private ProductService productService;
    private MediaService mediaService;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User createUser(User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // If someone is logged in (and not an anonymous user)
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            throw new ForbiddenException("Logged-in users cannot create new accounts");
        }

        checkEmailUniqueness(user);
        validateName(user.getName());
        user.setPassword(validatePassword(user.getPassword()));
        if (user.getRole() == null) {
            throw new IllegalArgumentException("Please select a role");
        }

        try {
            return userRepository.save(user);
        } catch (Exception e) {
            throw new IllegalArgumentException(e.getMessage());
            }
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

// Check if email is unique for that role, ignoring the user themselves
private void checkEmailUniqueness(User user) {
        Optional<User> existing = userRepository.findByEmailAndRole(user.getEmail(), user.getRole());

        if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
            String role = user.getRole().toString().toLowerCase();
            String message = String.format("%s with this email already exists", role);
            throw new IllegalArgumentException(message);
        }

}

private void validateName(String name) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Name cannot be null or empty");
        }
        if (name.length() < 2 || name.length() > 25) {
            throw new IllegalArgumentException("Name length must be between 2 and 25 characters");
        }
}

private String validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }
        if (password.length() < 3 || password.length() > 25) {
            throw new IllegalArgumentException("Password length must be between 3 and 25 characters");
        }

        return passwordEncoder.encode(password);

}

}
//service is responsible for business logic and data manipulation. It chooses how to handle data and interacts with the repository layer.
//it doesn't handle HTTP requests directly, that's the controller's job.
//Service can validate, filter, do calculations, and enforce business rules before passing data to/from the repository.
