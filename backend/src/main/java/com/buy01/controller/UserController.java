package com.buy01.controller;

import com.buy01.dto.SellerUpdateRequest;
import com.buy01.exception.ForbiddenException;
import com.buy01.exception.NotFoundException;
import com.buy01.model.Role;
import com.buy01.model.User;
import com.buy01.repository.UserRepository;
import com.buy01.service.MediaService;
import com.buy01.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import com.buy01.dto.UserResponseDTO;
import com.buy01.dto.UserUpdateRequest;

import java.util.Optional;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;
    private UserRepository userRepository;
    private MediaService mediaService;

    // finding user by id
    @GetMapping("/{userId}")
    public UserResponseDTO getUserById(@PathVariable String userId) {
        // check current user
        String currentUserId = userService.getCurrentUserId();

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
            @PathVariable String userId,
            @RequestBody UserUpdateRequest request) {
        // check current user
        String currentUserId = userService.getCurrentUserId();

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
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }

    // endpoint to get current logged-in user details (profile)
    @GetMapping("/me")
    public UserResponseDTO getCurrentUser() {
        String currentUserId = userService.getCurrentUserId();
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
    public UserResponseDTO updateCurrentUser(@RequestBody SellerUpdateRequest request) {
        String currentUserId = userService.getCurrentUserId();

        User user = userRepository.getUserById(currentUserId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        if (user.getRole() != Role.SELLER) {
            throw new ForbiddenException("Your role is not able to update the profile");
        }

        if (request.getAvatar() != null) {
            String avatarUrl = mediaService.saveUserAvatar(request.getAvatar());
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

}


