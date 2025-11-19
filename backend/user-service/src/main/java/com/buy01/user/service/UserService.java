package com.buy01.user.service;

import com.buy01.user.client.MediaClient;
import com.buy01.user.client.ProductClient;
import com.buy01.user.dto.*;
import com.buy01.user.exception.ForbiddenException;
import com.buy01.user.model.Role;
import com.buy01.user.model.User;
import com.buy01.user.repository.UserRepository;
import jakarta.ws.rs.InternalServerErrorException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserEventService userEventService;
    private final ProductClient productClient;
    private final MediaClient mediaClient;

    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, UserEventService userEventService, ProductClient productClient, MediaClient mediaClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEventService = userEventService;
        this.productClient = productClient;
        this.mediaClient = mediaClient;
    }

    // method to create user with validations
    public User createUser(User user, MultipartFile avatar) throws IOException {

        checkEmailUniqueness(user);
        validateName(user.getName());
        user.setPassword(validatePassword(user.getPassword()));

        if (user.getRole() == null) {
            throw new IllegalArgumentException("Please select a role");
        }

        User savedUser = userRepository.save(user);

        // 2. Only upload avatar if provided
        if (avatar != null && !avatar.isEmpty() && savedUser.getRole().equals(Role.SELLER)) {
            System.out.println("Avatar received in user service: " + avatar.getOriginalFilename());

            // Call media-service through MediaClient
            AvatarResponseDTO avatarResponseDTO =
                    mediaClient.saveAvatar(new AvatarCreateDTO(avatar));

            if (avatarResponseDTO != null) {
                savedUser.setAvatarUrl(avatarResponseDTO.getAvatarUrl());
                savedUser = userRepository.save(savedUser); // update with avatarUrl
            }
        }

        return savedUser;
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
        if (!role.equals("ADMIN") && !role.equals("SELLER")) {
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

    // updating user avatar by deleting the old one and uploading the new one
    public String updateUserAvatar(MultipartFile avatar, String oldAvatarUrl) throws IOException {
        // Call media-service through MediaClient
        AvatarResponseDTO avatarResponseDTO =
                mediaClient.updateAvatar(new AvatarUpdateRequest(oldAvatarUrl, avatar));

        if (avatarResponseDTO != null) {
            return avatarResponseDTO.getAvatarUrl();
        } else {
            throw new InternalServerErrorException("Failed to update avatar");
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
