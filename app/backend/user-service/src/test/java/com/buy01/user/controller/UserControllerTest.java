package com.buy01.user.controller;

import com.buy01.user.dto.ProductDTO;
import com.buy01.user.model.Role;
import com.buy01.user.model.User;
import com.buy01.user.repository.UserRepository;
import com.buy01.user.security.JwtUtil;
import com.buy01.user.security.SecurityUtils;
import com.buy01.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.data.mongodb.auto-index-creation=false",
        "spring.data.mongodb.database=test",
        "spring.data.mongodb.port=0",
        "spring.data.mongodb.host=localhost"
})
@ImportAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private SecurityUtils securityUtils;

    @MockBean
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        //userRepository.deleteAll();
    }

    // GET /api/users/me TESTS (for client and seller)
    @Test
    void getCurrentUser_asClient_returnsUserResponseDTO() throws Exception {
        User clientUser = new User("client1", "Client", "client@test.com", "pass", Role.CLIENT, null);
        //userRepository.save(clientUser);

        // Stub security utils to simulate logged-in user
        when(securityUtils.getCurrentUserId(anyString())).thenReturn("client1");
        when(securityUtils.getRole(anyString())).thenReturn("CLIENT");
        when(userService.findById("client1")).thenReturn(java.util.Optional.of(clientUser));

        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Client"))
                .andExpect(jsonPath("$.email").value("client@test.com"))
                .andExpect(jsonPath("$.products").doesNotExist());
    }

    @Test
    void getCurrentUser_asSeller_returnsSellerResponseDTO() throws Exception {
        User sellerUser = new User("seller1", "Seller", "seller@test.com", "pass", Role.SELLER, null);
        //userRepository.save(sellerUser);

        // Stub security utils
        when(securityUtils.getCurrentUserId(anyString())).thenReturn("seller1");
        when(securityUtils.getRole(anyString())).thenReturn("SELLER");
        when(userService.findById("seller1")).thenReturn(java.util.Optional.of(sellerUser));
        when(userService.getProductsForCurrentUser("seller1", "SELLER")).thenReturn(Collections.singletonList(new ProductDTO()));


        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Seller"))
                .andExpect(jsonPath("$.products").isArray());
    }

    // PUT /api/users/me TESTS (update avatar for seller)
    @Test
    void updateCurrentUser_asSeller_updatesAvatar() throws Exception {
        User sellerUser = new User("seller1", "Seller", "seller@test.com", "pass", Role.SELLER, null);
        //userRepository.save(sellerUser);

        when(securityUtils.getCurrentUserId(anyString())).thenReturn("seller1");
        when(securityUtils.getRole(anyString())).thenReturn("SELLER");

        when(userService.findById("seller1")).thenReturn(Optional.of(sellerUser));
        when(userService.updateUserAvatar(any(), anyString())).thenReturn("http://new-avatar.com/img.jpg");

        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar", "avatar.jpg", MediaType.IMAGE_JPEG_VALUE, "test-image".getBytes()
        );

        mockMvc.perform(multipart("/api/users/me")
                        .file(avatarFile)
                        .with(request -> { request.setMethod("PUT"); return request; })
                        .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatar").exists());
    }

    // GET /internal/user/{userId} TESTS
    @Test
    void getUserById_internal_returnsUserDTO() throws Exception {
        User user = new User("user123", "Test", "test@test.com", "pass", Role.CLIENT, null);
        //userRepository.save(user); - cannot use this in the tests, as it tries to connect to MongoDB

        when(userService.findById("user123")).thenReturn(Optional.of(user));

        mockMvc.perform(get("/internal/user/user123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user123"))
                .andExpect(jsonPath("$.role").value("CLIENT"));
    }

    // 404 when user not found
    @Test
    void getUserById_internal_whenNotFound_returns404() throws Exception {
        mockMvc.perform(get("/api/users/internal/user/nonexistent"))
                .andExpect(status().isNotFound());
    }
}
