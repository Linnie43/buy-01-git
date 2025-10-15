package com.buy01.controller;

import com.buy01.model.User;
import com.buy01.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.stream.Collectors;
import com.buy01.dto.UserResponse;
import com.buy01.dto.AdminUserResponse;
import com.buy01.dto.UpdateUserRequest;
import static com.buy01.security.SecurityUtils.isAdmin;


@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    // getting all the users with id, name and email, as only admin can access this endpoint
    @GetMapping
    public List<UserResponse> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return users.stream()
                .map(user -> new AdminUserResponse(user.getId(), user.getName(), user.getEmail()))
                .collect(Collectors.toList());
    }

    // finding user by id, only admin can access this endpoint
    @GetMapping("/{userId}")
    public AdminUserResponse getUserById(@PathVariable String userId) {
        if (!isAdmin()) {
            throw new RuntimeException("Forbidden - only admins can access this");
        }
        User user = userService.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new AdminUserResponse(user.getId(), user.getName(), user.getEmail());
    }

    // updating user by id, only admin can access this endpoint
    @PutMapping("/{userId}")
    public UserResponse updateUser(
            @PathVariable String userId,
            @RequestBody UpdateUserRequest request) {

        User updatedUser = userService.updateUser(userId, request);
        return new AdminUserResponse(updatedUser.getId(), updatedUser.getName(), updatedUser.getEmail());
    }


    // deleting user by id, only admin can access this endpoint
    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable String userId) {
        userService.deleteUser(userId);
    }

    // endpoint to get current logged in user details
    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        String currentUserId = userService.getCurrentUserId();
        User user = userService.findById(currentUserId).orElseThrow();
        return new UserResponse(user.getName(), user.getEmail());
    }

}


