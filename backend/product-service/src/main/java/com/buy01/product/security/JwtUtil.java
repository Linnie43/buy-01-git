package com.buy01.product.security;

import org.springframework.stereotype.Component;

@Component
public class JwtUtil {

    public String getToken(String authHeader) {
        return authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
    }

}
