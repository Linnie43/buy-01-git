package com.buy01.order.scheduler;

import com.buy01.order.dto.OrderUpdateRequest;
import com.buy01.order.model.OrderStatus;
import com.buy01.order.repository.OrderRepository;
import com.buy01.order.security.AuthDetails;
import com.buy01.order.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static com.buy01.order.scheduler.TestCartsAndOrders.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
class OrderStatusSchedulerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderService orderService;
    @InjectMocks
    private OrderStatusScheduler scheduler;

    @Test
    @DisplayName("Should progress order status from PENDING to next status")
    void shouldProgressStatus() {

        TestOrder order = order1();
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findAllByStatusNotIn(anySet()))
                .thenReturn(List.of(order));

        // Act
        scheduler.changeOrderStatuses();

        // Assert
        ArgumentCaptor<OrderUpdateRequest> captor = ArgumentCaptor.forClass(OrderUpdateRequest.class);
        verify(orderService).updateOrder(eq(order.getId()), captor.capture(), any(AuthDetails.class));

        // Check if the request contains the correct next status
        assertEquals("status should match", OrderStatus.CONFIRMED.getNextActiveStatus(), captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Should continue processing if one update fails")
    void shouldContinueOnException() {
        // Arrange: Two orders, the second one will fail
        TestOrder order1 = order1();
        TestOrder order2 = order2();
        order1.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findAllByStatusNotIn(anySet())).thenReturn(List.of(order1, order2));

        // Mock failure for the first call
        doThrow(new RuntimeException("DB Error"))
                .when(orderService).updateOrder(eq(order2.getId()), any(), any());

        // Act
        scheduler.changeOrderStatuses();

        // Assert: Verify order1 was still attempted despite order2's failure
        verify(orderService).updateOrder(eq(order1.getId()), any(), any());
    }
}
