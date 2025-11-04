package com.buy01.user.controller;

import com.buy01.user.dto.SellerUpdateRequest;
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
import com.buy01.user.dto.UserResponseDTO;
import com.buy01.user.dto.UserUpdateRequest;
import com.buy01.user.security.JwtUtil;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    private UserRepository userRepository;
    private JwtUtil jwtUtil;
    private SecurityUtils securityUtils;

    public UserController(UserRepository userRepository, JwtUtil jwtUtil, SecurityUtils securityUtils) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.securityUtils = securityUtils;
    }

    // finding user by id
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

    // endpoint to get current logged-in user details (profile)
    @GetMapping("/me")
    public UserResponseDTO getCurrentUser(
            @RequestHeader("Authorization") String authHeader
            ) {
        String currentUserId = securityUtils.getCurrentUserId(authHeader);
        User user = userService.findById(currentUserId).orElseThrow();

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

    @DeleteMapping("/me")
    public void deleteUser(@RequestHeader("Authorization") String authHeader) {
        String currentUserId = securityUtils.getCurrentUserId(authHeader);
        String token = jwtUtil.getToken(authHeader);
        userService.deleteUser(currentUserId, token);
    }
}


