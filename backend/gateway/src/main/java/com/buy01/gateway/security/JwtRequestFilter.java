package com.buy01.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import io.jsonwebtoken.Claims;
import java.util.List;

@Component
public class JwtRequestFilter implements WebFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private static final List<String> EXCLUDE_URLS = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/actuator/health"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();
        HttpMethod method = exchange.getRequest().getMethod();

        System.out.println("[DEBUG] Incoming request: " + exchange.getRequest().getMethod() + " " + path);

        boolean isExcluded = EXCLUDE_URLS.stream().anyMatch(path::startsWith)
                || ((path.startsWith("/api/products") || path.startsWith("/api/media")) && (method == HttpMethod.GET || method == HttpMethod.OPTIONS));

        if (isExcluded) {
            System.out.println("[DEBUG] Excluded URL, skipping JWT: " + path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        System.out.println("[DEBUG] Authorization header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                Claims claims = jwtUtil.extractClaims(token);
                String userId = claims.getSubject();
                String role = claims.get("role", String.class);

                System.out.println("[DEBUG] JWT claims extracted: userId=" + userId + ", role=" + role);

                if (userId != null) {
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                    var auth = new UsernamePasswordAuthenticationToken(userId, null, authorities);
                    return chain.filter(exchange)
                            .contextWrite(ReactiveSecurityContextHolder.withAuthentication(auth));
                } else {
                    System.out.println("[DEBUG] JWT has no subject");
                }
            } catch (Exception e) {
                System.out.println("[DEBUG] JWT validation failed: " + e.getMessage());
                exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return exchange.getResponse().setComplete();
            }
        } else {
            System.out.println("[DEBUG] No JWT token found");
        }

        return chain.filter(exchange);
    }
}
