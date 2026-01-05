package com.buy01.user.service;

import com.buy01.user.client.MediaClient;
import com.buy01.user.client.ProductClient;
import com.buy01.user.dto.*;
import com.buy01.user.exception.ForbiddenException;
import com.buy01.user.model.Role;
import com.buy01.user.model.User;
import com.buy01.user.repository.UserRepository;
import com.buy01.user.security.AuthDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserEventService userEventService;
    @Mock
    private ProductClient productClient;
    @Mock
    private MediaClient mediaClient;
    @Mock
    private MultipartFile avatar;

    private UserService userService;

    private BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Before each test, set up with dependencies
    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder,
                userEventService, productClient, mediaClient);
    }

    // Create User Tests

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void createUser_success_whenNoAvatar() throws IOException {
        UserCreateDTO user = new UserCreateDTO();
        user.setFirstname("Anna");
        user.setLastname("Test");
        user.setEmail("anna@test.com");
        user.setPassword("pass123");
        user.setRole(Role.CLIENT);

        when(userRepository.findByEmailAndRole("anna@test.com", Role.CLIENT))
                .thenReturn(Optional.empty());

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(user);

        assertNotNull(result);
        assertNotEquals("pass123", result.getPassword());
        assertTrue(passwordEncoder.matches("pass123", result.getPassword()));
        verify(mediaClient, never()).saveAvatar(any());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void createUser_success_withAvatar_whenSeller() throws IOException {
        UserCreateDTO user = new UserCreateDTO();
        user.setFirstname("Seller");
        user.setLastname("Test");
        user.setEmail("seller@test.com");
        user.setPassword("pass123");
        user.setRole(Role.SELLER);

        AvatarResponseDTO avatarResponse = new AvatarResponseDTO("http://avatar.com/myimg.png");

        when(avatar.isEmpty()).thenReturn(false);
        when(avatar.getOriginalFilename()).thenReturn("img.png");
        when(userRepository.findByEmailAndRole("seller@test.com", Role.SELLER))
                .thenReturn(Optional.empty());
        when(mediaClient.saveAvatar(any())).thenReturn(avatarResponse);
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.createUser(user);

        assertEquals("http://avatar.com/myimg.png", result.getAvatarUrl());
        assertTrue(passwordEncoder.matches("pass123", result.getPassword()));
        verify(mediaClient).saveAvatar(any());
    }

    @Test
    @MockitoSettings(strictness = Strictness.LENIENT)
    void createUser_throwsWhenEmailExists() {
        User existingUser = mock(User.class);
        when(existingUser.getId()).thenReturn("existing-id"); // mock ID since we cant get it when testing
        when(userRepository.findByEmail("exists@test.com"))
                .thenReturn(Optional.of(existingUser));

        UserCreateDTO newUser = new UserCreateDTO();
        newUser.setEmail("exists@test.com");
        newUser.setFirstname("Ebba");
        newUser.setLastname("Exists");
        newUser.setPassword("test123");
        newUser.setRole(Role.CLIENT);

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser(newUser));
    }

    // Get products tests

    @Test
    void getProductsForCurrentUser_validRole_callsProductService()throws IOException {
        when(productClient.getUsersProducts("123"))
                .thenReturn(List.of(new ProductDTO()));

        List<ProductDTO> result = userService.getProductsForCurrentUser(new AuthDetails("123", Role.SELLER));

        assertEquals(1, result.size());
        verify(productClient).getUsersProducts("123");
    }

    @Test
    void getProductsForCurrentUser_invalidRole_throws() {
        assertThrows(ForbiddenException.class,
                () -> userService.getProductsForCurrentUser(new AuthDetails("123", Role.CLIENT)));
    }

    // Update User Tests

    @Test
    void updateUser_updatesFields() {
        User existing = new User();
        existing.setName("Old");
        existing.setEmail("old@test.com");
        existing.setPassword("oldpass");

        UserUpdateRequest req = new UserUpdateRequest("New", "new@test.com", "pass123");

        when(userRepository.findById("1")).thenReturn(Optional.of(existing));
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        User result = userService.updateUser("1", req);

        assertEquals("New", result.getName());
        assertEquals("new@test.com", result.getEmail());
        assertTrue(passwordEncoder.matches("pass123", result.getPassword()));
    }

    // Deletion tests

    @Test
    void deleteUser_callsRepositoryAndPublishesEvent() {
        userService.deleteUser("123", new AuthDetails("123", Role.SELLER));

        verify(userRepository).deleteById("123");
        verify(userEventService).publishUserDeletedEvent("123");
    }
}
