package com.buy01.order.scheduler;

import com.buy01.order.dto.OrderUpdateRequest;
import com.buy01.order.model.Order;
import com.buy01.order.model.OrderStatus;
import com.buy01.order.model.Role;
import com.buy01.order.repository.OrderRepository;
import com.buy01.order.security.AuthDetails;
import com.buy01.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class OrderStatusScheduler {

    private final OrderRepository orderRepository;
    private final OrderService orderService;
    private final Logger log = LoggerFactory.getLogger(OrderStatusScheduler.class);

    @Scheduled(fixedRate = 60 * 1000) // run every 1 minute
    public void changeOrderStatuses() {
        AuthDetails currentUser = new AuthDetails("system", Role.ADMIN);
        List<Order> activeOrders = orderRepository.findAllByStatusNotIn(
                Set.of(OrderStatus.DELIVERED, OrderStatus.CANCELLED)
        );

        for (Order order : activeOrders) {
            OrderStatus currentStatus = order.getStatus();
            OrderStatus nextStatus = currentStatus.getNextActiveStatus();

            if (nextStatus != null) {
                try {
                    orderService.updateOrder(order.getId(), new OrderUpdateRequest(nextStatus), currentUser);
                } catch (Exception e) {
                    log.error("Failed to update order ID {} with status {}, error {}", order.getId(), nextStatus, e.getMessage());
                }
            }
        }
    }
}
