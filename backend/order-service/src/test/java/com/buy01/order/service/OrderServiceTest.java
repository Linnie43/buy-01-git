package com.buy01.order.service;

import com.buy01.order.model.Order;
import com.buy01.order.model.OrderItem;
import com.buy01.order.model.OrderStatus;
import com.buy01.order.model.ShippingAddress;
import com.buy01.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    static class TestOrder extends Order {
        TestOrder(String id, String userId, List<OrderItem> items, double totalPrice, OrderStatus status, ShippingAddress shippingAddress) {
            super(id, userId, items, totalPrice, status, shippingAddress);
        }
    }

    @BeforeEach
    void setUp() throws IOException {
        orderService = new OrderService(orderRepository);
    }

    @Test
    void createOrder_validRequest_returnsOrderResponseDTO() throws IOException {

        // Mock auth check

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
//            return new TestOrder();
            return null;
        });

        //OrderResponseDTO resp = orderService.createOrder(request, "SELLER", "current-user-1");

//        assertNotNull(resp);
//        assertEquals("prod-1", resp.getProductId());
//        assertEquals("Valid Name", resp.getName());
//        assertEquals("current-user-1", resp.getOwnerId());
    }

}
