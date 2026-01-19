package com.buy01.order.service;

import com.buy01.order.dto.*;
import com.buy01.order.exception.BadRequestException;
import com.buy01.order.exception.ForbiddenException;
import com.buy01.order.exception.NotFoundException;
import com.buy01.order.model.*;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.repository.OrderRepository;
import com.buy01.order.security.AuthDetails;
import io.jsonwebtoken.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.buy01.order.client.ProductClient;

import org.springframework.stereotype.Service;

import java.util.*;

// Service layer is responsible for business logic, validation, verification and data manipulation.
// It chooses how to handle data and interacts with the repository layer.
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartRepository cartRepository, ProductClient productClient) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    public OrderDashboardDTO getClientOrders(AuthDetails currentUser) {
        List<Order> orders = orderRepository.findOrdersByUserIdOrderByCreatedAtDesc(currentUser.getCurrentUserId());

        List<OrderResponseDTO> ordersDto = orders.stream()
                .map(this::mapToDTO)
                .toList();
        List<ItemDTO> topItems = orderRepository.findTopItemsByUserId(currentUser.getCurrentUserId(), 3);
        double totalSum = orders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(Order::getTotalPrice)
                .sum();


        return new OrderDashboardDTO(ordersDto, topItems, totalSum);
    }

    public OrderDashboardDTO getSellerOrders(AuthDetails currentUser) {
        List<Order> allOrders = orderRepository.findByItemsSellerId(currentUser.getCurrentUserId());

        List<OrderResponseDTO> sellerOrders = allOrders.stream()
                .map(order -> mapToSellerDTO(order, currentUser))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();

        List<ItemDTO> topItems = orderRepository.findTopItemsBySellerId(currentUser.getCurrentUserId(), 3);

        double totalSum = sellerOrders.stream()
                .filter(order -> order.getStatus() != OrderStatus.CANCELLED)
                .mapToDouble(OrderResponseDTO::getTotalPrice)
                .sum();

        return new OrderDashboardDTO(sellerOrders, topItems, totalSum);
    }

    public OrderResponseDTO getOrderById(String orderId, AuthDetails currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with orderId: " + orderId));

        log.info("getOrderById: {}", orderId);
        if (currentUser.getRole().equals(Role.SELLER)) {
            return mapToSellerDTO(order, currentUser)
                    .orElseThrow(() -> new ForbiddenException("Access denied to order with orderId: " + orderId + " for userId: " + currentUser.getCurrentUserId()));
        }

        if (!order.getUserId().equals(currentUser.getCurrentUserId()) && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new ForbiddenException("Access denied to order with orderId: " + orderId + " for userId: " + currentUser.getCurrentUserId());
        }

        return mapToDTO(order);
    }

    public OrderResponseDTO createOrder(OrderCreateDTO orderCreateDTO, AuthDetails currentUser) throws IOException {

        if (!currentUser.getRole().equals(Role.CLIENT)) {
            throw new BadRequestException("Current user is not a CLIENT and cannot place orders");
        }

        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId());
        if (cart == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Cannot create order.");
        }
        updateProductStock(cart.getItems());

        Order order = orderRepository.save(
                new Order(
                        currentUser.getCurrentUserId(),
                        cart.getItems(),
                        orderCreateDTO.getShippingAddress()
                )
        );

        // Clear the cart after order is placed
        cartRepository.delete(cart);

        return mapToDTO(order);
    }

    public OrderResponseDTO updateOrder(String orderId, OrderUpdateRequest orderUpdate, AuthDetails currentUser) throws IOException {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with orderId: " + orderId));

        if (!existingOrder.getUserId().equals(currentUser.getCurrentUserId())
                && !currentUser.getRole().equals(Role.ADMIN)) {
            throw new ForbiddenException("Access denied to update order with orderId: " + orderId + " for userId: " + currentUser.getCurrentUserId());
        }

        if (orderUpdate.getStatus() == null) {
            throw new BadRequestException("Order status cannot be null");
        }

        // Restrict clients: only allow CANCELLED
        if (currentUser.getRole().equals(Role.CLIENT)
                && orderUpdate.getStatus() != OrderStatus.CANCELLED) {
            throw new ForbiddenException("Clients can only cancel orders");
        }

        if (!existingOrder.getStatus().canTransitionTo(orderUpdate.getStatus())) {
            throw new BadRequestException(
                    "Order has status " + existingOrder.getStatus() +
                            " and cannot be updated to " + orderUpdate.getStatus()
            );
        }

        if (orderUpdate.getStatus() == OrderStatus.CANCELLED) {
            restoreProductStock(existingOrder.getItems());
        }

        existingOrder.setStatus(orderUpdate.getStatus());
        existingOrder.setUpdatedAt(new Date());
        if (orderUpdate.getStatus() == OrderStatus.SHIPPED) {
            addDeliveryDetails(existingOrder);
        } else if (orderUpdate.getStatus() == OrderStatus.DELIVERED) {
            existingOrder.setDeliveryDate(new Date());
        }

        return mapToDTO(orderRepository.save(existingOrder));
    }

    public void deleteOrderById(String orderId, AuthDetails currentUser) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with orderId: " + orderId));

        boolean isAdmin = currentUser.getRole().equals(Role.ADMIN);
        boolean isOwnerAndCreated = existingOrder.getUserId().equals(currentUser.getCurrentUserId())
                && existingOrder.getStatus().equals(OrderStatus.CREATED);

        if (!isAdmin && !isOwnerAndCreated) {
            throw new ForbiddenException(
                    "Only ADMIN can delete orders that have been confirmed. Order status is " + existingOrder.getStatus()
                            + ". Access denied for userId: " + currentUser.getCurrentUserId()
            );
        }
        restoreProductStock(existingOrder.getItems());
        orderRepository.delete(existingOrder);
    }

    // Helper methods

    // convert OrderItem to ItemDTO
    public ItemDTO toItemDTO(OrderItem item) {
        return new ItemDTO(
                item.getProductId(),
                item.getProductName(),
                item.getQuantity(),
                item.getPrice(),
                item.getPrice()*item.getQuantity(),
                item.getSellerId()
        );
    }

    // map Order to OrderResponseDTO
    private OrderResponseDTO mapToDTO(Order order) {
        return new OrderResponseDTO(
                order.getId(),
                order.getItems().stream()
                        .map(this::toItemDTO)
                        .toList(),
                order.getTotalPrice(),
                order.getStatus(),
                new ShippingAddressMaskedDTO(order.getShippingAddress()),
                order.isPaid(),
                order.getDeliveryDate(),
                order.getTrackingNumber(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

    private Optional<OrderResponseDTO> mapToSellerDTO(Order order, AuthDetails currentUser) {
        List<OrderItem> sellerItems = order.getItems().stream()
                .filter(item -> item.getSellerId().equals(currentUser.getCurrentUserId()))
                .toList();

        if (sellerItems.isEmpty()) {
            return Optional.empty();
        }

        List<ItemDTO> sellerItemDTOs = sellerItems.stream()
                .map(this::toItemDTO)
                .toList();

        double sellerTotal = sellerItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        OrderResponseDTO sellerOrderDTO = new OrderResponseDTO(
                order.getId(),
                sellerItemDTOs,
                sellerTotal,
                order.getStatus(),
                new ShippingAddressMaskedDTO(order.getShippingAddress()),
                order.isPaid(),
                order.getDeliveryDate(),
                order.getTrackingNumber(),
                order.getCreatedAt(),
                order.getUpdatedAt()
        );

        return Optional.of(sellerOrderDTO);
    }

    // update reserved quantity for each ordered product in product service
    private void updateProductStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            productClient.placeOrder(orderItem.getProductId(), -orderItem.getQuantity());
        }
    }

    public void restoreProductStock(List<OrderItem> orderItems) {
        for (OrderItem orderItem : orderItems) {
            productClient.cancelOrder(orderItem.getProductId(), orderItem.getQuantity());
        }
    }

    public void addDeliveryDetails(Order order) {
        String trackingNumber;
        Date deliveryDate;
            trackingNumber = "FKML" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            Calendar cal = Calendar.getInstance();
            cal.add(Calendar.DATE, 7); // add 7 days for delivery
            deliveryDate = cal.getTime();
            order.setTrackingNumber(trackingNumber);
            order.setDeliveryDate(deliveryDate);
    }

}
