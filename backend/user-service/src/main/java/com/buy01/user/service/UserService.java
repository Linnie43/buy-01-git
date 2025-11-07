package com.buy01.user.service;

import com.buy01.user.client.ProductClient;
import com.buy01.user.dto.ProductDTO;
import com.buy01.user.dto.UserUpdateRequest;
import com.buy01.user.exception.ForbiddenException;
import com.buy01.user.model.User;
import com.buy01.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import java.util.List;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import com.buy01.user.security.SecurityUtils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;
    private final UserEventService userEventService;
    private final ProductClient productClient;

    public UserService(UserRepository userRepository, RestTemplate restTemplate, BCryptPasswordEncoder passwordEncoder, SecurityUtils securityUtils, UserEventService userEventService, ProductClient productClient) {
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
        this.userEventService = userEventService;
        this.productClient = productClient;
    }

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

    public List<User> getAllUsers() {
        // validate admin role before fetching
        return userRepository.findAll();
    }

    // method to find user by id, needs validation what information is sent if own profile
    public Optional<User> findById(String userId) { // optional means it may or may not contain a non-null value
        return userRepository.findById(userId);
    }

    // method to find user by email, used in authentication
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<ProductDTO> getProductsForCurrentUser(String userId, String role) {
        // Call product-service to get products for the user
        if (!role.equals("ADMIN") || !role.equals("SELLER")) {
            throw new ForbiddenException("Invalid role to fetch products");
        }

        return productClient.getUsersProducts(userId);
    }

    // method to update user, only admin can update currently
    public User updateUser(String userId, UserUpdateRequest request) {

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

    public String updateUserAvatar(MultipartFile avatar, String oldAvatarUrl, String token) {
        try {
            // File as resource
            ByteArrayResource avatarResource = new ByteArrayResource(avatar.getBytes()) {
                @Override
                public String getFilename() {
                    return avatar.getOriginalFilename();
                }
            };

            // Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + token);

            // Request body (multipart)
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", avatarResource);
            body.add("oldAvatarUrl", oldAvatarUrl);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // PUT to media-service via gateway
            ResponseEntity<String> response = restTemplate.exchange(
                    "https://gateway:8443/api/media/avatar",
                    HttpMethod.PUT,
                    requestEntity,
                    String.class
            );

            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Avatar update failed: " + response.getStatusCode());
            }

            return response.getBody();

        } catch (Exception e) {
            throw new RuntimeException("Error updating avatar", e);
        }
    }

    // sending an API call for users products to be deleted and then deletes the user
    public void deleteUser(String userId, String token) {

        userRepository.deleteById(userId);
        userEventService.publishUserDeletedEvent(userId);
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

    // Validate name length (add validation for only alphabets)
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
