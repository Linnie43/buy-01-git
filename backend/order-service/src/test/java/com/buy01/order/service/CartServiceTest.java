package com.buy01.order.service;

import com.buy01.order.client.ProductClient;
import com.buy01.order.dto.*;
import com.buy01.order.exception.ForbiddenException;
import com.buy01.order.model.*;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;
import com.buy01.order.service.OrderServiceTest.TestCart;
import com.buy01.order.service.OrderServiceTest.TestOrder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.buy01.order.service.TestAuthFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Service
@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderService orderService;
    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    @Test
    @DisplayName("Get current cart when none exists creates a new cart")
    void getCurrentCartNull() throws Exception {
        when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(null);
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        Cart cart = cartService.getCurrentCart(clientUser());
        if (cart == null) {
            cart = cartService.createNewCart(clientUser());
        }

        assertNotNull(cart);
        assertEquals(clientUser().getCurrentUserId(), cart.getUserId());
        assertEquals(CartStatus.ACTIVE, cart.getCartStatus());
    }

    @Test
    @DisplayName("Get current cart for non-client user throws exception")
    void getCurrentCartFail() {
        assertThrows(Exception.class,
                () -> cartService.getCurrentCart(adminUser()));
    }

    @Test
    @DisplayName("Add to cart adds new item successfully")
    void addToCart() throws Exception {
        OrderServiceTest.TestCart cart = new TestCart(
                "cart1", clientUser().getCurrentUserId(), new ArrayList<>(), 0, CartStatus.ACTIVE);

        ProductUpdateDTO product = new ProductUpdateDTO(
                "p1", "Phone", 100.0, 10, ProductCategory.OTHER, sellerUser().getCurrentUserId());

        when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(productClient.getProductById("p1")).thenReturn(product);
        doNothing().when(productClient).updateQuantity(anyString(), anyInt());
        when(orderService.toItemDTO(any())).thenReturn(mock(ItemDTO.class));

        CartResponseDTO dto = cartService.addToCart(
                clientUser(), new CartItemRequestDTO("p1", 2));

        assertEquals(1, dto.getItems().size());
        assertEquals(200.0, dto.getTotalPrice());
    }

    @Test
    @DisplayName("Add to cart with insufficient stock throws exception")
    void addToCartNoStock() throws Exception {
        OrderServiceTest.TestCart cart = new TestCart(
                "cart1", clientUser().getCurrentUserId(), new ArrayList<>(), 0, CartStatus.ACTIVE);

        ProductUpdateDTO product = new ProductUpdateDTO(
                "p1", "Phone", 100.0, 1, ProductCategory.OTHER, sellerUser().getCurrentUserId());

        when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
        when(productClient.getProductById("p1")).thenReturn(product);

        assertThrows(Exception.class,
                () -> cartService.addToCart(
                        clientUser(), new CartItemRequestDTO("p1", 2)));
    }

    @Test
    @DisplayName("Update cart item quantity successfully")
    void updateCartAddToQuantity() throws Exception {
        OrderItem item = new OrderItem("p1", "Phone", 1, 100.0, sellerUser().getCurrentUserId());
        TestCart cart = new TestCart(
                "cart1", clientUser().getCurrentUserId(), new ArrayList<>(List.of(item)), 100, CartStatus.ACTIVE);

        when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(productClient).updateQuantity(anyString(), anyInt());
        when(orderService.toItemDTO(any())).thenReturn(mock(ItemDTO.class));

        CartResponseDTO dto =
                cartService.updateCart(clientUser(), "p1", new CartItemUpdateDTO(3));

        assertEquals(300.0, dto.getTotalPrice());
        verify(productClient).updateQuantity("p1", -2);
    }

    @Test
    @DisplayName("Update cart item quantity to remove items successfully")
    void updateCartReduceFromQuantity() throws Exception {
        OrderItem item = new OrderItem("p1", "Phone", 3, 100.0, sellerUser().getCurrentUserId());
        TestCart cart = new TestCart(
                "cart1", clientUser().getCurrentUserId(), new ArrayList<>(List.of(item)), 300, CartStatus.ACTIVE);

        when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(productClient).updateQuantity(anyString(), anyInt());
        when(orderService.toItemDTO(any())).thenReturn(mock(ItemDTO.class));

        CartResponseDTO dto =
                cartService.updateCart(clientUser(), "p1", new CartItemUpdateDTO(1));

        assertEquals(100.0, dto.getTotalPrice());
        verify(productClient).updateQuantity("p1", 2);
    }

    @Test
    @DisplayName("Delete item by ID removes item and returns stock")
    void deleteItemById() throws Exception {
        OrderItem item = new OrderItem("p1", "Phone", 2, 100.0, sellerUser().getCurrentUserId());
        TestCart cart = new TestCart(
                "cart1", clientUser().getCurrentUserId(), new ArrayList<>(List.of(item)), 200, CartStatus.ACTIVE);

        when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
        doNothing().when(productClient).updateQuantity(anyString(), anyInt());

        cartService.deleteItemById("p1", clientUser());

        assertTrue(cart.getItems().isEmpty());
        verify(productClient).updateQuantity("p1", 2);
        verify(cartRepository).delete(cart);
    }

    @Test
    @DisplayName("Add to cart from order copies items successfully")
    void addToCartFromOrderSuccessful() throws Exception {
        TestOrder order = new TestOrder(
                "o1", clientUser().getCurrentUserId(),
                List.of(
                        new OrderItem("p1", "Phone", 2, 100.0, sellerUser().getCurrentUserId()),
                        new OrderItem("p2", "Laptop", 1, 500.0, sellerUser2().getCurrentUserId())
                ),
                700,
                OrderStatus.DELIVERED,
                null);

        TestCart cart = new TestCart(
                "cart1", clientUser().getCurrentUserId(), new ArrayList<>(), 0, CartStatus.ACTIVE);

        when(orderRepository.getOrderById("o1")).thenReturn(Optional.of(order));
        when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        when(productClient.getProductById("p1")).thenReturn(
                new ProductUpdateDTO("p1", "Phone", 100.0, 1, ProductCategory.OTHER, sellerUser().getCurrentUserId()));
        when(productClient.getProductById("p2")).thenReturn(
                new ProductUpdateDTO("p2", "Laptop", 500.0, 5, ProductCategory.OTHER, sellerUser2().getCurrentUserId()));
        when(orderService.toItemDTO(any())).thenReturn(mock(ItemDTO.class));
        doNothing().when(productClient).updateQuantity(anyString(), anyInt());

        CartResponseDTO dto = cartService.addToCartFromOrder(clientUser(), "o1");

        assertEquals(2, dto.getItems().size());
        assertEquals(600.0, dto.getTotalPrice()); // p1 quantity adjusted to 1
    }

    @Test
    @DisplayName("Add to cart from order for another user throws forbidden")
    void addToCartFromOrderForbidden() throws Exception {
        TestOrder order = new TestOrder(
                "o1", "otherUser", List.of(), 0,
                OrderStatus.DELIVERED, null);

        when(orderRepository.getOrderById("o1")).thenReturn(Optional.of(order));

        assertThrows(ForbiddenException.class,
                () -> cartService.addToCartFromOrder(clientUser(), "o1"));
    }

}
