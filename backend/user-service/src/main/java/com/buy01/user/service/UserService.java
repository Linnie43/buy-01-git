package com.buy01.user.service;

import com.buy01.user.client.MediaClient;
import com.buy01.user.client.ProductClient;
import com.buy01.user.dto.*;
import com.buy01.user.exception.ForbiddenException;
import com.buy01.user.exception.NotFoundException;
import com.buy01.user.model.Role;
import com.buy01.user.model.User;
import com.buy01.user.repository.UserRepository;
import com.buy01.user.security.AuthDetails;
import jakarta.ws.rs.InternalServerErrorException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final UserEventService userEventService;
    private final ProductClient productClient;
    private final MediaClient mediaClient;
    private static final Logger log = LoggerFactory.getLogger(UserService.class);


    public UserService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, UserEventService userEventService, ProductClient productClient, MediaClient mediaClient) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userEventService = userEventService;
        this.productClient = productClient;
        this.mediaClient = mediaClient;
    }

    // method to create user with validations
    public User createUser(UserCreateDTO newUser) throws IOException {

        checkEmailUniqueness(newUser.getEmail(), "");
        String name = newUser.getFirstname().trim() + " " + newUser.getLastname().trim();
        validateName(name);
        newUser.setPassword(validatePassword(newUser.getPassword().trim()));

        if (newUser.getRole() == null) {
            throw new IllegalArgumentException("Please select a role");
        }

        AvatarResponseDTO avatarResponseDTO = null;

        if (newUser.getAvatar() != null && !newUser.getAvatar().isEmpty() && newUser.getRole().equals(Role.SELLER)) {
            log.info("Avatar received in user service: {}", newUser.getAvatar().getOriginalFilename());

            // Call media-service through MediaClient
             avatarResponseDTO = mediaClient.saveAvatar(new AvatarCreateDTO(newUser.getAvatar()));

        }

        // Create the user entity
        User user = new User();
        user.setName(name);
        user.setEmail(newUser.getEmail().toLowerCase().trim());
        user.setPassword(newUser.getPassword());
        user.setRole(newUser.getRole());

        if (avatarResponseDTO != null) {
            user.setAvatarUrl(avatarResponseDTO.getAvatarUrl());
        }

        // Save the user only if the avatar upload is successful or no avatar is provided
        user.setCreateTime(new Date());
        user.setUpdateTime(new Date());
        return userRepository.save(user);
    }

    public UserResponseDTO getCurrentUser(AuthDetails currentUser) throws IOException {

        User user = findById(currentUser.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (currentUser.getRole().equals("ADMIN") || currentUser.getRole().equals("SELLER")) {
            log.info("Fetching products for user with role: {}", currentUser.getRole());
            // get products from product service
            List<ProductDTO> products = getProductsForCurrentUser(currentUser);
            return new SellerResponseDTO(user, products);
        }

        return new UserResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl(),
                true
        );
    }

    public UserResponseDTO updateCurrentUser(AuthDetails currentUser, SellerUpdateRequest request) throws IOException {
        User user = userRepository.findUserByUserId(currentUser.getCurrentUserId())
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() != Role.SELLER && user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Your role is not able to update the profile");
        }

        if (request.getAvatar() != null) {
            String oldAvatar = user.getAvatarUrl() != null ? user.getAvatarUrl() : "";
            String avatarUrl = updateUserAvatar(request.getAvatar(), oldAvatar);
            user.setAvatarUrl(avatarUrl);
        }
        user.setUpdateTime(new Date());
        userRepository.save(user);

        return new UserResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl(),
                true
        );
    }

    // method to find user by id, needs validation what information is sent if own profile
    public Optional<User> findById(String userId) { // optional means it may or may not contain a non-null value
        return userRepository.findById(userId);
    }

    // method to find user by email, used in authentication
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public List<ProductDTO> getProductsForCurrentUser(AuthDetails currentUser) throws IOException {
        // Call product-service to get products for the user
        if (!currentUser.getRole().equals("ADMIN") && !currentUser.getRole().equals("SELLER")) {
            throw new ForbiddenException("Invalid role to fetch products");
        }

        return productClient.getUsersProducts(currentUser.getCurrentUserId());
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

        existingUser.setUpdateTime(new Date());
        return userRepository.save(existingUser);
    }

    // updating user avatar by deleting the old one and uploading the new one
    public String updateUserAvatar(MultipartFile avatar, String oldAvatarUrl) throws IOException {
        // Call media-service through MediaClient
        AvatarResponseDTO avatarResponseDTO =
                mediaClient.updateAvatar(new AvatarUpdateRequest(oldAvatarUrl, avatar));

        if (avatarResponseDTO != null) {
            log.info("avatarResponseDTO filled: {}",  avatarResponseDTO);
            return avatarResponseDTO.getAvatarUrl();
        } else {
            throw new InternalServerErrorException("Failed to update avatar");
        }
    }

    // sending an API call for users products to be deleted and then deletes the user
    public void deleteUser(String userId, AuthDetails currentUser) {

        if (!currentUser.getCurrentUserId().equals(userId) || !currentUser.getRole().equals("ADMIN")) {
            throw new ForbiddenException("You don't have permission to delete this user");
        }

        userRepository.deleteById(userId);
        userEventService.publishUserDeletedEvent(userId);
    }

    // Check if email is unique for that role, ignoring the user themselves
    private void checkEmailUniqueness(String email, String userId) {
            Optional<User> existing = userRepository.findByEmail(email);

            if (existing.isPresent() && !existing.get().getId().equals(userId)) {
                String message = "User with this email already exists";
                throw new IllegalArgumentException(message);
            }

    }

    // Validate name length (add validation for only alphabets)
    private void validateName(String name) {
        name = name.trim();
            if (name.isEmpty()) {
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
