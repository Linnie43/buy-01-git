package com.buy01.user.controller;

import com.buy01.user.dto.*;
import com.buy01.user.exception.ForbiddenException;
import com.buy01.user.exception.NotFoundException;
import com.buy01.user.model.Role;
import com.buy01.user.model.User;
import com.buy01.user.repository.UserRepository;
import com.buy01.user.security.AuthDetails;
import com.buy01.user.security.SecurityUtils;
import com.buy01.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.buy01.user.security.JwtUtil;

import java.io.IOException;
import java.util.Date;
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

    // getting user by id, currently not used (can be used to show other user's profile)
//    @GetMapping("/{userId}")
//    public UserResponseDTO getUserById(
//            @RequestHeader("Authorization") String authHeader,
//            @PathVariable String userId) {
//        // check current user
//        String currentUserId = securityUtils.getCurrentUserId(authHeader);
//
//        User user = userService.findById(userId)
//                .orElseThrow(() -> new NotFoundException("User not found"));
//        return new UserResponseDTO(
//                user.getName(),
//                user.getEmail(),
//                user.getRole(),
//                user.getAvatarUrl(),
//                currentUserId.equals(userId)
//        );
//    }

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
            ) throws IOException {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        return userService.getCurrentUser(currentUser);
    }

    // endpoint for seller to update their avatar
    @PutMapping("/me")
    public UserResponseDTO updateCurrentUser(
            @RequestHeader("Authorization") String authHeader,
            @ModelAttribute @Valid SellerUpdateRequest request
    ) throws IOException {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        return userService.updateCurrentUser(currentUser, request);

    }

    // updating user by id, only admin
//    @PutMapping("/{userId}")
//    public UserResponseDTO updateUser(
//            @RequestHeader("Authorization") String authHeader,
//            @PathVariable String userId,
//            @RequestBody UserUpdateRequest request) {
//        // check current user
//        String currentUserId = securityUtils.getCurrentUserId(authHeader);
//
//        User updatedUser = userService.updateUser(userId, request);
//        return new UserResponseDTO(
//                updatedUser.getName(),
//                updatedUser.getEmail(),
//                updatedUser.getRole(),
//                updatedUser.getAvatarUrl(),
//                currentUserId.equals(userId)
//
//        );
//    }

    // endpoint for user to delete their own account
//    @DeleteMapping("/me")
//    public void deleteUser(@RequestHeader("Authorization") String authHeader) {
//        String currentUserId = securityUtils.getCurrentUserId(authHeader);
//        String token = jwtUtil.getToken(authHeader);
//        userService.deleteUser(currentUserId, authHeader);
//    }

    // deleting user by id, only admin can access this endpoint
//    @DeleteMapping("/{userId}")
//    public void deleteUser(@PathVariable String userId, Authentication auth,
//                           @RequestHeader("Authorization") String authHeader) {
//        // Check if user has ADMIN role
//        if (auth == null || auth.getAuthorities().stream()
//                .noneMatch(a -> a.getAuthority().equals("ADMIN"))) {
//            throw new ForbiddenException("Forbidden - only admin can delete users");
//        }
//
//        // Forward JWT token for internal calls if needed
//        String token = jwtUtil.getToken(authHeader);
//
//        // Call service method
//        userService.deleteUser(userId, authHeader);
//    }

}


