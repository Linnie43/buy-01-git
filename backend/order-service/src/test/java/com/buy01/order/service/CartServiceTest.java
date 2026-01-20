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
import com.buy01.order.service.TestAuthFactory.TestCart;
import com.buy01.order.service.TestAuthFactory.TestOrder;

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
                TestCart cart = new TestCart(
                                cart1().getId(), clientUser().getCurrentUserId(), new ArrayList<>(), 0,
                                CartStatus.ACTIVE);

                ProductUpdateDTO product = new ProductUpdateDTO(
                                product1().getProductId(), product1().getProductName(), product1().getPrice(), product1().getQuantity(), ProductCategory.OTHER,
                                product1().getSellerId());

                when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
                when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
                when(productClient.getProductById(product1().getProductId())).thenReturn(product);
                doNothing().when(productClient).updateQuantity(anyString(), anyInt());
                when(orderService.toItemDTO(any())).thenReturn(mock(ItemDTO.class));

                CartResponseDTO dto = cartService.addToCart(
                                clientUser(), new CartItemRequestDTO(product1().getProductId(), product1().getQuantity()));

                assertEquals(1, dto.getItems().size());
                assertEquals(100.0, dto.getTotalPrice());
        }

        @Test
        @DisplayName("Add to cart with insufficient stock throws exception")
        void addToCartNoStock() {
                TestCart cart = new TestCart(
                                cart1().getId(), clientUser().getCurrentUserId(), new ArrayList<>(), 0,
                                CartStatus.ACTIVE);

                ProductUpdateDTO product = new ProductUpdateDTO(
                                product1().getProductId(), product1().getProductName(), product1().getPrice(), product1().getQuantity(), ProductCategory.OTHER,
                                product1().getSellerId());

                when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
                when(productClient.getProductById(product1().getProductId())).thenReturn(product);

                assertThrows(Exception.class,
                                () -> cartService.addToCart(
                                                clientUser(), new CartItemRequestDTO(product1().getProductId(), 3)));
        }

        @Test
        @DisplayName("Update cart item quantity successfully")
        void updateCartAddToQuantity() throws Exception {
                OrderItem item = product1();
                TestCart cart = new TestCart(
                                cart1().getId(), clientUser().getCurrentUserId(), new ArrayList<>(List.of(item)), 100,
                                CartStatus.ACTIVE);

                when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
                when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
                doNothing().when(productClient).updateQuantity(anyString(), anyInt());
                when(orderService.toItemDTO(any())).thenReturn(mock(ItemDTO.class));

                CartResponseDTO dto = cartService.updateCart(clientUser(), product1().getProductId(),
                                new CartItemUpdateDTO(3));

                assertEquals(150.0, dto.getTotalPrice());
                verify(productClient).updateQuantity(product1().getProductId(), -1);
        }

        @Test
        @DisplayName("Update cart item quantity to remove items successfully")
        void updateCartReduceFromQuantity() throws Exception {
                TestCart cart = new TestCart(
                                cart1().getId(), clientUser().getCurrentUserId(), new ArrayList<>(List.of(product1())), 100,
                                CartStatus.ACTIVE);

                when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
                when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
                doNothing().when(productClient).updateQuantity(anyString(), anyInt());
                when(orderService.toItemDTO(any())).thenReturn(mock(ItemDTO.class));

                CartResponseDTO dto = cartService.updateCart(clientUser(), product1().getProductId(),
                                new CartItemUpdateDTO(1));

                assertEquals(50.0, dto.getTotalPrice());
                verify(productClient).updateQuantity(product1().getProductId(), 1);
        }

        @Test
        @DisplayName("Delete item by ID removes item and returns stock")
        void deleteItemById() {

                TestCart cart = new TestCart(
                                cart1().getId(), clientUser().getCurrentUserId(), new ArrayList<>(List.of(product1())), 100,
                                CartStatus.ACTIVE);

                when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
                doNothing().when(productClient).updateQuantity(anyString(), anyInt());

                cartService.deleteItemById(product1().getProductId(), clientUser());

                assertTrue(cart.getItems().isEmpty());
                verify(productClient).updateQuantity(product1().getProductId(), 2);
                verify(cartRepository).delete(cart);
        }

        @Test
        @DisplayName("Add to cart from order copies items successfully")
        void addToCartFromOrderSuccessful() throws Exception {

                TestCart cart = new TestCart(
                                cart1().getId(), clientUser().getCurrentUserId(), new ArrayList<>(), 0,
                                CartStatus.ACTIVE);

                when(orderRepository.getOrderById(order1().getId())).thenReturn(Optional.of(order1()));
                when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);
                when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));
                when(productClient.getProductById(product1().getProductId())).thenReturn(
                                new ProductUpdateDTO(product1().getProductId(), product1().getProductName(), product1().getPrice(), 1,
                                                ProductCategory.OTHER,
                                                product1().getSellerId()));
                when(productClient.getProductById(product2().getProductId())).thenReturn(
                                new ProductUpdateDTO(product2().getProductId(), product2().getProductName(), product2().getPrice(), product2().getQuantity(),
                                                ProductCategory.OTHER,
                                                product2().getSellerId()));
                when(orderService.toItemDTO(any())).thenReturn(mock(ItemDTO.class));
                doNothing().when(productClient).updateQuantity(anyString(), anyInt());

                CartResponseDTO dto = cartService.addToCartFromOrder(clientUser(), order1().getId());

                assertEquals(2, dto.getItems().size());
                assertEquals(80.0, dto.getTotalPrice());
        }

        @Test
        @DisplayName("Add to cart from order for another user throws forbidden")
        void addToCartFromOrderForbidden() {
                TestOrder order = new TestOrder(
                                "o1", "otherUser", List.of(product3()), 60.0,
                                OrderStatus.DELIVERED, shippingAddress());

                when(orderRepository.getOrderById("o1")).thenReturn(Optional.of(order));

                assertThrows(ForbiddenException.class,
                                () -> cartService.addToCartFromOrder(clientUser(), "o1"));
        }

}
