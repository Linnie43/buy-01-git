package com.buy01.order.service;

import com.buy01.order.dto.OrderCreateDTO;
import com.buy01.order.dto.OrderResponseDTO;
import com.buy01.order.dto.OrderUpdateRequest;
import com.buy01.order.exception.ForbiddenException;
import com.buy01.order.model.*;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.repository.OrderRepository;
import com.buy01.order.security.AuthDetails;
import jakarta.ws.rs.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

import static com.buy01.order.model.OrderStatus.CANCELED;
import static com.buy01.order.model.OrderStatus.SHIPPED;
import static com.buy01.order.service.TestAuthFactory.clientUser;
import static com.buy01.order.service.TestAuthFactory.sellerUser;
import static com.buy01.order.service.TestAuthFactory.adminUser;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Service
@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private OrderService orderService;

    static class TestOrder extends Order {
        TestOrder(String id, String userId, List<OrderItem> items, double totalPrice, OrderStatus status, ShippingAddress shippingAddress) {
            super(id, userId, items, totalPrice, status, shippingAddress);
        }
    }

    static class TestCart extends Cart {
        TestCart(String id, String userId, List<OrderItem> items, double totalPrice, CartStatus cartStatus) {
            super(id, userId, items, totalPrice, cartStatus);
        }
    }

    @Test
    void getClientOrders_existingOrders_returnsOrderResponseDTOList() throws IOException {
        // Mock auth check
        AuthDetails currentUser = clientUser();

        when(orderRepository.findOrdersByUserId("user1")).thenReturn(List.of(
                new TestOrder("order1", "user1", List.of(new OrderItem()), 100.0, OrderStatus.CREATED, null),
                new TestOrder("order2", "user1", List.of(), 200.0, SHIPPED, null)
        ));

        List<OrderResponseDTO> orders = orderService.getClientOrders(currentUser);

        assertEquals(2, orders.size(), "Expected 2 orders for the client");
        OrderResponseDTO order = orders.get(0);
        assertEquals("order1", order.getOrderId(), "Order ID should match");
        assertEquals(100.0, order.getTotalPrice(), "Total price should match");
        assertEquals(OrderStatus.CREATED, order.getStatus(), "Order Status should match");
        assertEquals(1, order.getItems().size(), "Order items size should match");

        verify(orderRepository, times(1)).findOrdersByUserId("user1");
    }

    @Test
    void getSellerOrders_existingOrders_returnsOrderResponseDTOList() throws IOException {
        // Mock auth check
        AuthDetails currentUser = sellerUser();

        when(orderRepository.findByItemsSellerId("seller1")).thenReturn(List.of(
                new TestOrder("order1", "user1", List.of(
                        new OrderItem("prod1", "Product 1", 2, 50.0, "seller1"),
                        new OrderItem("prod2", "Product 2", 1, 30.0, "seller2")
                ), 130.0, OrderStatus.CREATED, null),
                new TestOrder("order2", "user2", List.of(
                        new OrderItem("prod3", "Product 3", 3, 20.0, "seller1")
                ), 60.0, SHIPPED, null)
        ));

        List<OrderResponseDTO> orders = orderService.getSellerOrders(currentUser);

        assertEquals(2, orders.size(), "Expected 2 orders for the seller");

        OrderResponseDTO order1 = orders.get(0);
        assertEquals("order1", order1.getOrderId(), "Order ID should match");
        assertEquals(100.0, order1.getTotalPrice(), "Total price should match");
        assertEquals(OrderStatus.CREATED, order1.getStatus(), "Order Status should match");
        assertEquals(1, order1.getItems().size(), "Order items size should match for seller");

        OrderResponseDTO order2 = orders.get(1);
        assertEquals("order2", order2.getOrderId(), "Order ID should match");
        assertEquals(60.0, order2.getTotalPrice(), "Total price should match");
        assertEquals(SHIPPED, order2.getStatus(), "Order Status should match");
        assertEquals(1, order2.getItems().size(), "Order items size should match for seller");

        verify(orderRepository, times(1)).findByItemsSellerId("seller1");
    }

    @Test
    void getOrderById_existingOrderAsClient_returnsOrderResponseDTO() throws IOException {
        // Mock auth check
        AuthDetails currentUser = clientUser();

        when(orderRepository.findById("order1")).thenReturn(java.util.Optional.of(
                new TestOrder("order1", "user1", List.of(
                        new OrderItem("prod1", "Product 1", 2, 50.0, "seller1"),
                        new OrderItem("prod2", "Product 2", 1, 30.0, "seller2")
                ), 130.0, OrderStatus.CREATED, null)
        ));
        OrderResponseDTO order = orderService.getOrderById("order1", currentUser);
        assertEquals("order1", order.getOrderId(), "Order ID should match");
        assertEquals(130.0, order.getTotalPrice(), "Total price should match");
        assertEquals(OrderStatus.CREATED, order.getStatus(), "Order Status should match");
        assertEquals(2, order.getItems().size(), "Order items size should match");
        verify(orderRepository, times(1)).findById("order1");
    }

    @Test
    void createOrder_validCart_createsOrderSuccessfully() throws IOException {
        // Mock auth check
        AuthDetails currentUser = clientUser();

        when(cartRepository.findByUserId("user1")).thenReturn(
                new TestCart("cart1", "user1", List.of(
                        new OrderItem("prod1", "Product 1", 2, 50.0, "seller1"),
                        new OrderItem("prod2", "Product 2", 1, 30.0, "seller2")
                ), 130.0, CartStatus.CHECKOUT
        ));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return new TestOrder("order1", order.getUserId(), order.getItems(), order.getTotalPrice(), order.getStatus(), order.getShippingAddress());
        });

        OrderResponseDTO orderResponse = orderService.createOrder(new OrderCreateDTO(), currentUser);

        assertEquals("order1", orderResponse.getOrderId(), "Order ID should match");
        assertEquals(130.0, orderResponse.getTotalPrice(), "Total price should match");
        assertEquals(OrderStatus.CREATED, orderResponse.getStatus(), "Order Status should match");
        assertEquals(2, orderResponse.getItems().size(), "Order items size should match");

        verify(cartRepository, times(1)).findByUserId("user1");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrder_validUpdate_updatesOrderSuccessfully() throws IOException {
        // Mock auth check
        AuthDetails currentUser = clientUser();

        TestOrder existingOrder = new TestOrder("order1", "user1", List.of(
                new OrderItem("prod1", "Product 1", 2, 50.0, "seller1")
        ), 100.0, OrderStatus.CONFIRMED, null);

        when(orderRepository.findById("order1")).thenReturn(java.util.Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderUpdateRequest updateRequest = new OrderUpdateRequest(CANCELED);

        OrderResponseDTO updatedOrder = orderService.updateOrder("order1", updateRequest, currentUser);

        assertEquals("order1", updatedOrder.getOrderId(), "Order ID should match");
        assertEquals(updateRequest.getStatus(), updatedOrder.getStatus(), "Order Status should be updated " + updateRequest.getStatus());

        verify(orderRepository, times(1)).findById("order1");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrder_forbiddenUpdate_throwsForbiddenException() throws IOException {
        // Mock auth check
        AuthDetails currentUser = clientUser();

        TestOrder existingOrder = new TestOrder("order1", "user1", List.of(
                new OrderItem("prod1", "Product 1", 2, 50.0, "seller1")
        ), 100.0, SHIPPED, null);

        when(orderRepository.findById("order1")).thenReturn(java.util.Optional.of(existingOrder));

        OrderUpdateRequest updateRequest = new OrderUpdateRequest(OrderStatus.CANCELED);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> orderService.updateOrder("order1", updateRequest, currentUser),
                "Expected BadRequestException for invalid status transition"
        );

        assertTrue(exception.getMessage().contains("cannot be updated"),
                "Exception message should indicate invalid transition");

        // Verify repository save was never called
        verify(orderRepository, never()).save(any());
        verify(orderRepository, times(1)).findById("order1");
    }

    @Test
    void deleteOrder_validDelete_deletesOrderSuccessfully() throws IOException {
        // Mock auth check
        AuthDetails currentUser = adminUser();

        TestOrder existingOrder = new TestOrder("order1", "user1", List.of(
                new OrderItem("prod1", "Product 1", 2, 50.0, "seller1")
        ), 100.0, OrderStatus.CONFIRMED, null);

        when(orderRepository.findById("order1")).thenReturn(java.util.Optional.of(existingOrder));
        orderService.deleteOrderById("order1", currentUser);
        verify(orderRepository, times(1)).findById("order1");
        verify(orderRepository, times(1)).delete(existingOrder);
    }

    @Test
    void deleteOrder_forbiddenDelete_throwsForbiddenException() throws IOException {
        // Mock auth check
        AuthDetails currentUser = clientUser();

        TestOrder existingOrder = new TestOrder("order1", "user1", List.of(
                new OrderItem("prod1", "Product 1", 2, 50.0, "seller1")
        ), 100.0, OrderStatus.CONFIRMED, null);

        when(orderRepository.findById("order1")).thenReturn(java.util.Optional.of(existingOrder));
        // Act & Assert
        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> orderService.deleteOrderById("order1", currentUser),
                "Expected ForbiddenException for unauthorized delete"
        );
        assertTrue(exception.getMessage().contains("Only ADMIN can delete orders"),
                "Exception message should indicate forbidden delete");
        // Verify repository delete was never called
        verify(orderRepository, never()).delete(any());
        verify(orderRepository, times(1)).findById("order1");
    }

}
