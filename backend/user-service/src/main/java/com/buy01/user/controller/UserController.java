package com.buy01.user.controller;

import com.buy01.user.dto.*;
import com.buy01.user.exception.NotFoundException;
import com.buy01.user.model.User;
import com.buy01.user.repository.UserRepository;
import com.buy01.user.security.AuthDetails;
import com.buy01.user.security.SecurityUtils;
import com.buy01.user.service.UserService;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;
import com.buy01.user.security.JwtUtil;

import java.io.IOException;


@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    public UserController(UserService userService, UserRepository userRepository, SecurityUtils securityUtils) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.securityUtils = securityUtils;
    }

    // internal endpoint for other services to get user by id
    @GetMapping("/internal/user/{userId}")
    public UserDTO getUserById(@PathVariable String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new UserDTO(user.getId(), user.getRole());
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

}


