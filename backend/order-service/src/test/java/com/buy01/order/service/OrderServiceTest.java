package com.buy01.order.service;

import com.buy01.order.client.ProductClient;
import com.buy01.order.dto.*;
import com.buy01.order.exception.ForbiddenException;
import com.buy01.order.model.*;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.repository.OrderRepository;
import com.buy01.order.security.AuthDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.buy01.order.exception.BadRequestException;
import com.buy01.order.service.TestAuthFactory.TestCart;
import com.buy01.order.service.TestAuthFactory.TestOrder;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.buy01.order.service.TestAuthFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Service
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

        @Mock
        private OrderRepository orderRepository;
        @Mock
        private CartRepository cartRepository;
        @Mock
        private ProductClient productClient;

        @InjectMocks
        private OrderService orderService;

        @Test
        @DisplayName("Get client dashboard returns order list, top items, and total spent")
        void getClientDashboard() {
                // Mock auth check
                AuthDetails currentUser = clientUser();

                when(orderRepository.findOrdersByUserIdOrderByCreatedAtDesc(clientUser().getCurrentUserId()))
                                .thenReturn(List.of(
                                                order1(),
                                                order2(),
                                                new TestOrder("order3", clientUser().getCurrentUserId(), List.of(product1(), product3()),
                                                                160.0, OrderStatus.CANCELLED, shippingAddress())));
                when(orderRepository.findTopItemsByUserId(currentUser.getCurrentUserId(), 3)).thenReturn(List.of(
                                new ItemDTO(product1().getProductId(), product1().getProductName(), product1().getQuantity(), product1().getPrice(), product1().getPrice()*product1().getQuantity(),
                                                product1().getSellerId()),
                                new ItemDTO(product2().getProductId(), product2().getProductName(), product2().getQuantity(), product2().getPrice(), product2().getPrice()*product2().getQuantity(),
                                                product2().getSellerId())));

                OrderDashboardDTO orderDashboard = orderService.getClientOrders(currentUser);

                assertEquals(3, orderDashboard.getOrders().size(), "Expected 3 orders for the client");
                assertEquals(190.0, orderDashboard.getTotal(), "Total spent should match");
                assertEquals(product1().getProductId(), orderDashboard.getTopItems().get(0).getProductId(),
                                "Top item ID should match");

                OrderResponseDTO order = orderDashboard.getOrders().get(0);
                assertEquals(order1().getId(), order.getOrderId(), "Order ID should match");
                assertEquals(order1().getTotalPrice(), order.getTotalPrice(), "Total price should match");
                assertEquals(order1().getStatus(), order.getStatus(), "Order Status should match");
                assertEquals(order1().getItems().size(), order.getItems().size(), "Order items size should match");

                verify(orderRepository, times(1))
                                .findOrdersByUserIdOrderByCreatedAtDesc(clientUser().getCurrentUserId());
        }

        @Test
        @DisplayName("Get seller dashboard returns order list, top items, and total revenue")
        void getSellerDashboard() {
                // Mock auth check
                AuthDetails currentUser = sellerUser();

                when(orderRepository.findByItemsSellerId(sellerUser().getCurrentUserId())).thenReturn(List.of(
                                order1(),
                                order2()));
                when(orderRepository.findTopItemsBySellerId(sellerUser().getCurrentUserId(), 3)).thenReturn(List.of(
                                new ItemDTO(product3().getProductId(), product3().getProductName(), product3().getQuantity(),
                                        product3().getPrice(), product3().getPrice()*product3().getQuantity(), product3().getSellerId()),
                                new ItemDTO(product1().getProductId(), product1().getProductName(), product1().getQuantity(),
                                        product1().getPrice(), product1().getPrice()*product1().getQuantity(), product1().getSellerId())));

                OrderDashboardDTO orderDashboard = orderService.getSellerOrders(currentUser);

                assertEquals(2, orderDashboard.getOrders().size(), "Expected 2 orders for the seller");
                assertEquals(160.0, orderDashboard.getTotal(), "Total revenue should match");
                assertEquals(product3().getProductId(), orderDashboard.getTopItems().get(0).getProductId(), "Top item ID should match");

                OrderResponseDTO order1 = orderDashboard.getOrders().get(0);
                assertEquals(order1().getId(), order1.getOrderId(), "Order ID should match");
                assertEquals(100.0, order1.getTotalPrice(), "Total price should match");
                assertEquals(order1().getStatus(), order1.getStatus(), "Order Status should match");
                assertEquals(1, order1.getItems().size(), "Order items size should match for seller");

                OrderResponseDTO order2 = orderDashboard.getOrders().get(1);
                assertEquals(order2().getId(), order2.getOrderId(), "Order ID should match");
                assertEquals(order2().getTotalPrice(), order2.getTotalPrice(), "Total price should match");
                assertEquals(order2().getStatus(), order2.getStatus(), "Order Status should match");
                assertEquals(order2().getItems().size(), order2.getItems().size(), "Order items size should match for seller");

                verify(orderRepository, times(1)).findByItemsSellerId(sellerUser().getCurrentUserId());
        }

        @Test
        @DisplayName("Get order by ID returns order details")
        void getOrderById() {
                // Mock auth check
                AuthDetails currentUser = clientUser();

                when(orderRepository.findById(order1().getId())).thenReturn(java.util.Optional.of(order1()));
                OrderResponseDTO order = orderService.getOrderById(order1().getId(), currentUser);
                assertEquals(order1().getId(), order.getOrderId(), "Order ID should match");
                assertEquals(order1().getTotalPrice(), order.getTotalPrice(), "Total price should match");
                assertEquals(order1().getStatus(), order.getStatus(), "Order Status should match");
                assertEquals(order1().getItems().size(), order.getItems().size(), "Order items size should match");
                verify(orderRepository, times(1)).findById(order1().getId());
        }

        @Test
        @DisplayName("Create order from valid cart creates order successfully")
        void createOrder() {
                // Mock auth check
                AuthDetails currentUser = clientUser();
                TestCart cart = cart1();
                cart.setCartStatus(CartStatus.CHECKOUT);

                when(cartRepository.findByUserId(clientUser().getCurrentUserId())).thenReturn(cart);

                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
                        Order order = invocation.getArgument(0);
                        return new TestOrder(order1().getId(), order.getUserId(), order.getItems(),
                                        order.getTotalPrice(),
                                        order.getStatus(), shippingAddress());
                });

                OrderResponseDTO orderResponse = orderService.createOrder(new OrderCreateDTO(), currentUser);

                assertEquals(order1().getId(), orderResponse.getOrderId(), "Order ID should match");
                assertEquals(order1().getTotalPrice(), orderResponse.getTotalPrice(), "Total price should match");
                assertEquals(OrderStatus.CREATED, orderResponse.getStatus(), "Order Status should match");
                assertEquals(2, orderResponse.getItems().size(), "Order items size should match");

                verify(cartRepository, times(1)).findByUserId(clientUser().getCurrentUserId());
                verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Update order with valid data updates order successfully")
        void updateOrder() {
                // Mock auth check
                AuthDetails currentUser = clientUser();

                TestOrder existingOrder = order1();
                existingOrder.setStatus(OrderStatus.CREATED);

                when(orderRepository.findById(order1().getId())).thenReturn(java.util.Optional.of(existingOrder));
                when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

                OrderUpdateRequest updateRequest = new OrderUpdateRequest(OrderStatus.CANCELLED);

                OrderResponseDTO updatedOrder = orderService.updateOrder(order1().getId(), updateRequest, currentUser);

                assertEquals(order1().getId(), updatedOrder.getOrderId(), "Order ID should match");
                assertEquals(updateRequest.getStatus(), updatedOrder.getStatus(),
                                "Order Status should be updated " + updateRequest.getStatus());

                verify(orderRepository, times(1)).findById(order1().getId());
                verify(orderRepository, times(1)).save(any(Order.class));
        }

        @Test
        @DisplayName("Update order with forbidden status update throws exception")
        void updateOrderForbidden() {
                // Mock auth check
                AuthDetails currentUser = clientUser();

                when(orderRepository.findById(order1().getId())).thenReturn(java.util.Optional.of(order1()));

                OrderUpdateRequest updateRequest = new OrderUpdateRequest(OrderStatus.CANCELLED);

                // Act & Assert
                BadRequestException exception = assertThrows(
                                BadRequestException.class,
                                () -> orderService.updateOrder(order1().getId(), updateRequest, currentUser),
                                "Expected BadRequestException for invalid status transition");

                assertTrue(exception.getMessage().contains("cannot be updated"),
                                "Exception message should indicate invalid transition");

                // Verify repository save was never called
                verify(orderRepository, never()).save(any());
                verify(orderRepository, times(1)).findById(order1().getId());
        }

        @Test
        @DisplayName("Delete order by ID deletes order successfully for admin")
        void deleteOrder() {
                // Mock auth check
                AuthDetails currentUser = adminUser();

                TestOrder existingOrder = order1();

                when(orderRepository.findById(order1().getId())).thenReturn(java.util.Optional.of(existingOrder));
                orderService.deleteOrderById(order1().getId(), currentUser);
                verify(orderRepository, times(1)).findById(order1().getId());
                verify(orderRepository, times(1)).delete(existingOrder);
        }

        @Test
        @DisplayName("Delete order by ID forbidden for non-admin users")
        void deleteOrderForbidden() {
                // Mock auth check
                AuthDetails currentUser = clientUser();

                TestOrder existingOrder = order1();

                when(orderRepository.findById(order1().getId())).thenReturn(java.util.Optional.of(existingOrder));
                // Act & Assert
                ForbiddenException exception = assertThrows(
                                ForbiddenException.class,
                                () -> orderService.deleteOrderById(order1().getId(), currentUser),
                                "Expected ForbiddenException for unauthorized delete");
                assertTrue(exception.getMessage().contains("Only ADMIN can delete orders"),
                                "Exception message should indicate forbidden delete");
                // Verify repository delete was never called
                verify(orderRepository, never()).delete(any());
                verify(orderRepository, times(1)).findById(order1().getId());
        }

}
