package com.buy01.media.controller;

import com.buy01.media.repository.MediaRepository;
import com.buy01.media.service.MediaService;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {
        "eureka.client.enabled=false",
        "spring.cloud.discovery.enabled=false",
        "spring.data.mongodb.auto-index-creation=false",
        "spring.data.mongodb.database=test",
        "spring.data.mongodb.port=0",
        "spring.data.mongodb.host=localhost"
})
public class MediaControllerTest {

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private MediaService mediaService;

    @Autowired
    private MediaRepository mediaRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        mediaRepository.deleteAll(); // clean DB before each test
    }

    // --- POST /api/media/internal/images TEST ---
    @Test
    void uploadImage_andGetProductImages() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "files", "test.png", "image/png", "dummy content".getBytes()
        );

        // Upload image
        mockMvc.perform(multipart("/api/media/internal/images")
                        .file(file)
                        .param("productId", "product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // Verify the uploaded image via controller endpoint
        mockMvc.perform(get("/api/media/internal/images/productId/{productId}", "product-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].productId").value("product-1"));
    }

    // --- DELETE /api/media/internal/images/{id} TEST ---
    @Test
    void deleteImage_shouldRemoveFromRepository() throws Exception {
        // First save image via service
        MockMultipartFile file = new MockMultipartFile(
                "files", "test.png", "image/png", "dummy".getBytes()
        );

        var uploaded = mediaService.saveProductImages("product-1", List.of(file));
        String mediaId = uploaded.get(0).getId();

        // Delete the image
        mockMvc.perform(delete("/api/media/internal/images/{id}", mediaId))
                .andExpect(status().isOk());

        // Ensure it's deleted
        boolean exists = mediaRepository.findById(mediaId).isPresent();
        assert !exists;
    }

    // --- GET /api/media/internal/avatar & /api/media/avatar/{filename} TEST (avatar get and serve) ---
    @Test
    void uploadAvatar_andGetAvatar() throws Exception {
        MockMultipartFile avatarFile = new MockMultipartFile(
                "avatar", "avatar.png", "image/png", "dummy avatar".getBytes()
        );

        var response = mockMvc.perform(multipart("/api/media/internal/avatar")
                        .file(avatarFile))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.avatarUrl").exists())
                .andReturn();

        String avatarUrl = JsonPath.read(response.getResponse().getContentAsString(), "$.avatarUrl");

        // Request avatar
        mockMvc.perform(get("/api/media/avatar/{filename}", avatarUrl))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

}
