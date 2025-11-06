package com.buy01.gateway.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.http.HttpMethod;
import java.util.List;


@Configuration
@EnableMethodSecurity
public class GatewaySecurityConfig {

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .httpBasic(httpBasic -> httpBasic.disable())
                .formLogin(form -> form.disable())
                .authorizeExchange(auth -> auth
                        // public endpoints
                        .pathMatchers("/user-service/api/auth/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/product-service/api/products/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/media-service/api/media/images/**").permitAll()

                        // User endpoints
                        .pathMatchers("/user-service/api/users/me").hasAnyRole("CLIENT", "SELLER")
                        .pathMatchers("/user-service/api/users/**").hasRole("ADMIN") // includes /users and /users/{id}

                        // Product endpoints
                        .pathMatchers("/product-service/api/products/my-products").hasRole("SELLER")
                        .pathMatchers(HttpMethod.POST, "/product-service/api/products/**").hasAnyRole("SELLER", "ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/product-service/api/products/**").hasAnyRole("SELLER", "ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/product-service/api/products/**").hasAnyRole("SELLER", "ADMIN")

                        // Media endpoints
                        .pathMatchers("/media-service/api/media/avatar/**").hasAnyRole("SELLER", "ADMIN")
                        .pathMatchers("/media-service/api/media/images/**").hasAnyRole("SELLER", "ADMIN")

                        .anyExchange().authenticated()
                )
                .addFilterAt(jwtRequestFilter, SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        System.out.println("CorsConfigurationSource activated");
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("https://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        System.out.println("Source after cors: " +  source);
        return source;
    }
}
