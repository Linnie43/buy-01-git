package com.buy01.user.controller;
import com.buy01.user.exception.NotFoundException;
import com.buy01.user.security.JwtUtil;
import com.buy01.user.security.SecurityUtils;
import com.buy01.user.service.UserService;
import org.springframework.web.bind.annotation.*;
import com.buy01.user.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.IOException;
import java.util.Map;
import jakarta.validation.Valid;
import com.buy01.user.dto.UserCreateDTO;
import com.buy01.user.dto.UserResponseDTO;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SecurityUtils securityUtils;

    public AuthController(UserService userService, JwtUtil jwtUtil, BCryptPasswordEncoder passwordEncoder, SecurityUtils securityUtils) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
        this.passwordEncoder = passwordEncoder;
        this.securityUtils = securityUtils;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
        String email = loginData.get("email");
        String password = loginData.get("password");

        User user = userService.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // in case password does not match
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user);
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/signup")
    public ResponseEntity<?> signup(
            @RequestHeader(value = "Authorization", required = false) String authHeader ,
            @ModelAttribute @Valid UserCreateDTO request // accepting avatar at signup
    ) throws IOException {
        final String currentUserId = (authHeader != null) ? securityUtils.getCurrentUserId(authHeader) : null;

        if (currentUserId != null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Logged-in users cannot create new accounts");
        }

        System.out.println("Received DTO: " + request);
        User user = new User();
        user.setName(request.getFirstname() + " " + request.getLastname());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());
        user.setRole(request.getRole());

        User created = userService.createUser(user, request.getAvatar());
        UserResponseDTO response = new UserResponseDTO(created.getName(), created.getEmail(), created.getRole(), null, null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }



}


