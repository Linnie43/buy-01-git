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

    public AuthDetails getAuthDetails(String authHeader) {

        String userId;
        String role;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.extractClaims(token);
                userId = claims.getSubject();
                role = claims.get("role", String.class);
            } catch (Exception e) {
                throw new ForbiddenException("Invalid JWT token", e);
            }
        } else {
            throw new ForbiddenException("User not authenticated");
        }

        return new AuthDetails(userId, role);

    }

}
