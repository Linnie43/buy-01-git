package com.buy01.order.security;

import com.buy01.order.exception.ForbiddenException;
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
                System.out.println("FAILS IN GET USERID");
                throw new ForbiddenException("Invalid JWT token", e);
            }
        }

        if (userId == null) {
            throw new ForbiddenException("User not authenticated");
        }

        return userId;

    }

    //if the user is admin
    public String getRole(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return "";
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = jwtUtil.extractClaims(token);
            return claims.get("role", String.class);
        } catch (Exception e) {
            return "";
        }
    }
}
