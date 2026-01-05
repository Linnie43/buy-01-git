package com.buy01.product.security;

import com.buy01.product.exception.ForbiddenException;
import com.buy01.product.model.Role;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {

    private final JwtUtil jwtUtil;

    public SecurityUtils(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    public AuthDetails getAuthDetails(String authHeader) {

        String userId;
        Role role;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.extractClaims(token);
                userId = claims.getSubject();
                String roleStr = claims.get("role", String.class);

                try {
                    role = Role.valueOf(roleStr); // maps String to enum
                } catch (IllegalArgumentException e) {
                    throw new ForbiddenException("Invalid role in JWT: " + roleStr, e);
                }

            } catch (Exception e) {
                throw new ForbiddenException("Invalid JWT token", e);
            }
        } else {
            throw new ForbiddenException("User not authenticated");
        }

        return new AuthDetails(userId, role);

    }
}
