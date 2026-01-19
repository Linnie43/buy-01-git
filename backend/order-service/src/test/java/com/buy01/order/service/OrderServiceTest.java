package com.buy01.order.service;

import com.buy01.order.client.ProductClient;
import com.buy01.order.dto.*;
import com.buy01.order.exception.ForbiddenException;
import com.buy01.order.model.*;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.repository.OrderRepository;
import com.buy01.order.security.AuthDetails;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import com.buy01.order.exception.BadRequestException;

import java.io.IOException;
import java.util.List;

import static com.buy01.order.model.OrderStatus.SHIPPED;
import static com.buy01.order.service.TestAuthFactory.clientUser;
import static com.buy01.order.service.TestAuthFactory.sellerUser;
import static com.buy01.order.service.TestAuthFactory.adminUser;
import static com.buy01.order.service.TestAuthFactory.shippingAddress;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

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

    static class TestOrder extends Order {
        TestOrder(String id, String userId, List<OrderItem> items, double totalPrice, OrderStatus status,
                ShippingAddress shippingAddress) {
            super(id, userId, items, totalPrice, status, shippingAddress);
        }
    }

    static class TestCart extends Cart {
        TestCart(String id, String userId, List<OrderItem> items, double totalPrice, CartStatus cartStatus) {
            super(id, userId, items, totalPrice, cartStatus);
        }
    }

    @Test
    void getClientDashboard_returnsOrderList_topItems_totalSpent() throws IOException {
        // Mock auth check
        AuthDetails currentUser = clientUser();

        when(orderRepository.findOrdersByUserIdOrderByCreatedAtDesc("user1")).thenReturn(List.of(
                new TestOrder("order1", "user1", List.of(new OrderItem()), 100.0, OrderStatus.CREATED, shippingAddress()),
                new TestOrder("order2", "user1", List.of(), 200.0, SHIPPED, shippingAddress()),
                new TestOrder("order3", "user1", List.of(), 150.0, OrderStatus.CANCELLED, shippingAddress())));
        when(orderRepository.findTopItemsByUserId(currentUser.getCurrentUserId(), 3)).thenReturn(List.of(
                new ItemDTO("prod1", "Product 1", 3, 50.0, 150.0, "seller1"),
                new ItemDTO("prod2", "Product 2", 2, 30.0, 60.0, "seller2")
        ));

        OrderDashboardDTO orderDashboard = orderService.getClientOrders(currentUser);

        assertEquals(3, orderDashboard.getOrders().size(), "Expected 2 orders for the client");
        assertEquals(300.0, orderDashboard.getTotal(), "Total spent should match");
        assertEquals("prod1", orderDashboard.getTopItems().get(0).getProductId(), "Top item ID should match");

        OrderResponseDTO order = orderDashboard.getOrders().get(0);
        assertEquals("order1", order.getOrderId(), "Order ID should match");
        assertEquals(100.0, order.getTotalPrice(), "Total price should match");
        assertEquals(OrderStatus.CREATED, order.getStatus(), "Order Status should match");
        assertEquals(1, order.getItems().size(), "Order items size should match");

        verify(orderRepository, times(1)).findOrdersByUserIdOrderByCreatedAtDesc("user1");
    }

    @Test
    void getSellerDashboard_returnsOrderList_topItems_totalRevenue() throws IOException {
        // Mock auth check
        AuthDetails currentUser = sellerUser();

        when(orderRepository.findByItemsSellerId("seller1")).thenReturn(List.of(
                new TestOrder("order1", "user1", List.of(
                        new OrderItem("prod1", "Product 1", 2, 50.0, "seller1"),
                        new OrderItem("prod2", "Product 2", 1, 30.0, "seller2")), 130.0, OrderStatus.CREATED, shippingAddress()),
                new TestOrder("order2", "user2", List.of(
                        new OrderItem("prod3", "Product 3", 3, 20.0, "seller1")), 60.0, OrderStatus.SHIPPED, shippingAddress())));
        when(orderRepository.findTopItemsBySellerId("seller1", 3)).thenReturn(List.of(
                new ItemDTO("prod3", "Product 3", 3, 20.0, 60.0, "seller1"),
                new ItemDTO("prod1", "Product 1", 2, 50.0, 100.0, "seller1")
        ));

        OrderDashboardDTO orderDashboard = orderService.getSellerOrders(currentUser);

        assertEquals(2, orderDashboard.getOrders().size(), "Expected 2 orders for the seller");
        assertEquals(160.0, orderDashboard.getTotal(), "Total revenue should match");
        assertEquals("prod3", orderDashboard.getTopItems().get(0).getProductId(), "Top item ID should match");

        OrderResponseDTO order1 = orderDashboard.getOrders().get(0);
        assertEquals("order1", order1.getOrderId(), "Order ID should match");
        assertEquals(100.0, order1.getTotalPrice(), "Total price should match");
        assertEquals(OrderStatus.CREATED, order1.getStatus(), "Order Status should match");
        assertEquals(1, order1.getItems().size(), "Order items size should match for seller");

        OrderResponseDTO order2 = orderDashboard.getOrders().get(1);
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
                        new OrderItem("prod2", "Product 2", 1, 30.0, "seller2")), 130.0, OrderStatus.CREATED, shippingAddress())));
        OrderResponseDTO order = orderService.getOrderById("order1", currentUser);
        assertEquals("order1", order.getOrderId(), "Order ID should match");
        assertEquals(130.0, order.getTotalPrice(), "Total price should match");
        assertEquals(OrderStatus.CREATED, order.getStatus(), "Order Status should match");
        assertEquals(2, order.getItems().size(), "Order items size should match");
        verify(orderRepository, times(1)).findById("order1");
    }

    @Test
    void createOrder_validCart_createsOrderSuccessfully() {
        // Mock auth check
        AuthDetails currentUser = clientUser();

        when(cartRepository.findByUserId("user1")).thenReturn(
                new TestCart("cart1", "user1", List.of(
                        new OrderItem("prod1", "Product 1", 2, 50.0, "seller1"),
                        new OrderItem("prod2", "Product 2", 1, 30.0, "seller2")), 130.0, CartStatus.CHECKOUT));

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order order = invocation.getArgument(0);
            return new TestOrder("order1", order.getUserId(), order.getItems(), order.getTotalPrice(),
                    order.getStatus(), shippingAddress());
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
                new OrderItem("prod1", "Product 1", 2, 50.0, "seller1")), 100.0, OrderStatus.CONFIRMED, shippingAddress());

        when(orderRepository.findById("order1")).thenReturn(java.util.Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> invocation.getArgument(0));

        OrderUpdateRequest updateRequest = new OrderUpdateRequest(OrderStatus.CANCELLED);

        OrderResponseDTO updatedOrder = orderService.updateOrder("order1", updateRequest, currentUser);

        assertEquals("order1", updatedOrder.getOrderId(), "Order ID should match");
        assertEquals(updateRequest.getStatus(), updatedOrder.getStatus(),
                "Order Status should be updated " + updateRequest.getStatus());

        verify(orderRepository, times(1)).findById("order1");
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void updateOrder_forbiddenUpdate_throwsForbiddenException() throws IOException {
        // Mock auth check
        AuthDetails currentUser = clientUser();

        TestOrder existingOrder = new TestOrder("order1", "user1", List.of(
                new OrderItem("prod1", "Product 1", 2, 50.0, "seller1")), 100.0, SHIPPED, shippingAddress());

        when(orderRepository.findById("order1")).thenReturn(java.util.Optional.of(existingOrder));

        OrderUpdateRequest updateRequest = new OrderUpdateRequest(OrderStatus.CANCELLED);

        // Act & Assert
        BadRequestException exception = assertThrows(
                BadRequestException.class,
                () -> orderService.updateOrder("order1", updateRequest, currentUser),
                "Expected BadRequestException for invalid status transition");

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
                new OrderItem("prod1", "Product 1", 2, 50.0, "seller1")), 100.0, OrderStatus.CONFIRMED, shippingAddress());

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
                new OrderItem("prod1", "Product 1", 2, 50.0, "seller1")), 100.0, OrderStatus.CONFIRMED, shippingAddress());

        when(orderRepository.findById("order1")).thenReturn(java.util.Optional.of(existingOrder));
        // Act & Assert
        ForbiddenException exception = assertThrows(
                ForbiddenException.class,
                () -> orderService.deleteOrderById("order1", currentUser),
                "Expected ForbiddenException for unauthorized delete");
        assertTrue(exception.getMessage().contains("Only ADMIN can delete orders"),
                "Exception message should indicate forbidden delete");
        // Verify repository delete was never called
        verify(orderRepository, never()).delete(any());
        verify(orderRepository, times(1)).findById("order1");
    }

}
