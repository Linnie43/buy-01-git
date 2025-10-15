package com.buy01.controller;

import com.buy01.model.User;
import com.buy01.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import com.buy01.dto.UserResponseDTO;
import com.buy01.dto.SellerCreateDTO;
import com.buy01.dto.UserUpdateDTO;
import static com.buy01.security.SecurityUtils.isAdmin;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // finding user by id
    @GetMapping("/{userId}")
    public UserResponseDTO getUserById(@PathVariable String userId) {

        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserResponseDTO(
                user.getName(),
                user.getEmail()
        );
    }

    // updating user by id, only update own id
    @PutMapping("/{userId}")
    public UserResponseDTO updateUser(
            @PathVariable String userId,
            @RequestBody UserUpdateDTO request) {

        User updatedUser = userService.updateUser(userId, request);
        return new UserResponseDTO(
                updatedUser.getName(),
                updatedUser.getEmail()
        );
    }


    // deleting user by id, only admin can access this endpoint
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }

    // endpoint to get current logged in user details
    @GetMapping("/me")
    public UserResponseDTO getCurrentUser() {
        String currentUserId = userService.getCurrentUserId();
        User user = userService.findById(currentUserId).orElseThrow();
        return new UserResponseDTO(user.getName(), user.getEmail());
    }

}


