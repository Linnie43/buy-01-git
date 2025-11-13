package com.buy01.user.controller;

import com.buy01.user.dto.*;
import com.buy01.user.exception.ForbiddenException;
import com.buy01.user.exception.NotFoundException;
import com.buy01.user.model.Role;
import com.buy01.user.model.User;
import com.buy01.user.repository.UserRepository;
import com.buy01.user.security.SecurityUtils;
import com.buy01.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.buy01.user.security.JwtUtil;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final SecurityUtils securityUtils;

    public UserController(UserService userService, UserRepository userRepository, JwtUtil jwtUtil, SecurityUtils securityUtils) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.securityUtils = securityUtils;
    }

    // getting user by id
    @GetMapping("/{userId}")
    public UserResponseDTO getUserById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId) {
        // check current user
        String currentUserId = securityUtils.getCurrentUserId(authHeader);

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl(),
                currentUserId.equals(userId)
        );
    }

    // internal endpoint for other services to get user by id
    @GetMapping("/internal/user/{userId}")
    public UserDTO getUserById(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new UserDTO(user.getId(), user.getRole().toString());
    }


    // endpoint for the user to get their own details (profile)
    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(
            @RequestHeader("Authorization") String authHeader
            ) {
        System.out.println("Getting current user profile");
        String currentUserId = securityUtils.getCurrentUserId(authHeader);
        String role = securityUtils.getRole(authHeader);
        System.out.println("Current user ID: " + currentUserId + ", role: " + role);

        User user = userService.findById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (role.equals("ADMIN") || role.equals("SELLER")) {
            System.out.println("Fetching products for user with role: " + role);
            // get products from product service
            List<ProductDTO> products = userService.getProductsForCurrentUser(currentUserId, role);
            return new SellerResponseDTO(user, products);
        }

        System.out.println("Fetching profile for user with role: " + role);
        return new UserResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl(),
                true
        );
    }

    // endpoint for seller to update their avatar
    @PutMapping("/me")
    public UserResponseDTO updateCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @RequestBody SellerUpdateRequest request
    ) {
        String currentUserId = securityUtils.getCurrentUserId(authHeader);

        User user = userRepository.findUserById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() != Role.SELLER) {
            throw new ForbiddenException("Your role is not able to update the profile");
        }

        String token = jwtUtil.getToken(authHeader);

        if (request.getAvatar() != null) {
            String oldAvatar = user.getAvatarUrl();
            String avatarUrl = userService.updateUserAvatar(request.getAvatar(), oldAvatar, token);
            user.setAvatarUrl(avatarUrl);
        }
        userRepository.save(user);

        return new UserResponseDTO(
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getAvatarUrl(),
                true
        );

    }

    // updating user by id, only admin
    @PutMapping("/{userId}")
    public UserResponseDTO updateUser(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String userId,
            @RequestBody UserUpdateRequest request) {
        // check current user
        String currentUserId = securityUtils.getCurrentUserId(authHeader);

        User updatedUser = userService.updateUser(userId, request);
        return new UserResponseDTO(
                updatedUser.getName(),
                updatedUser.getEmail(),
                updatedUser.getRole(),
                updatedUser.getAvatarUrl(),
                currentUserId.equals(userId)

        );
    }

    // endpoint for user to delete their own account
    @DeleteMapping("/me")
    public void deleteUser(@RequestHeader("Authorization") String authHeader) {
        String currentUserId = securityUtils.getCurrentUserId(authHeader);
        String token = jwtUtil.getToken(authHeader);
        userService.deleteUser(currentUserId, token);
    }

    // deleting user by id, only admin can access this endpoint
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId, Authentication auth,
                           @RequestHeader("Authorization") String authHeader) {
        // Check if user has ADMIN role
        if (auth == null || auth.getAuthorities().stream()
                .noneMatch(a -> a.getAuthority().equals("ADMIN"))) {
            throw new RuntimeException("Forbidden - only admin can delete users");
        }

        // Forward JWT token for internal calls if needed
        String token = jwtUtil.getToken(authHeader);

        // Call service method
        userService.deleteUser(userId, token);
    }

}


