package com.buy01.user.security;

import com.buy01.user.exception.ForbiddenException;
import com.buy01.user.security.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final JwtUtil jwtUtil;

    public SecurityUtils(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public String getCurrentUserId(String authHeader) {
        String userId = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.extractClaims(token); // you can use same JwtUtil as in Gateway
                userId = claims.getSubject();
            } catch (Exception e) {
                throw new ForbiddenException("Invalid JWT token", e);
            }
        }

        if (userId == null) {
            throw new ForbiddenException("User not authenticated");
        }

        return userId;

    }

    //get the current role of the user
    public String getRole(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "";
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.extractClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            System.out.println("FAILS IN GET ROLE " + e.getMessage());
            return "";
        }
    }
}
