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

import org.springframework.stereotype.Service;

import java.util.*;

// Service layer is responsible for business logic, validation, verification and data manipulation.
// It chooses how to handle data and interacts with the repository layer.
@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, CartRepository cartRepository) {
        this.orderRepository = orderRepository;
        this.cartRepository = cartRepository;
    }

    public OrderDashboardDTO getClientOrders(AuthDetails currentUser) {
        List<Order> orders = orderRepository.findOrdersByUserId(currentUser.getCurrentUserId());

        List<OrderResponseDTO> ordersDto = orders.stream()
                .map(this::mapToDTO)
                .toList();
        List<ItemDTO> topItems = orderRepository.findTopItemsByUserId(currentUser.getCurrentUserId(), 3);
        double totalSum = orders.stream()
                .mapToDouble(Order::getTotalPrice)
                .sum();


        return new OrderDashboardDTO(ordersDto, topItems, totalSum);
    }

    public OrderDashboardDTO getSellerOrders(AuthDetails currentUser) {
        List<Order> orders = orderRepository.findByItemsSellerId(currentUser.getCurrentUserId());

        List<OrderResponseDTO> sellerOrders = orders.stream()
                .map(order -> filterOrderForSeller(order, currentUser)) // filter items for the current seller
                .map(this::mapToDTO)
                .toList();

        List<ItemDTO> topItems = orderRepository.findTopItemsBySellerId(currentUser.getCurrentUserId(), 3);
        double totalSum = sellerOrders.stream()
                .mapToDouble(OrderResponseDTO::getTotalPrice)
                .sum();

        return new OrderDashboardDTO(sellerOrders, topItems, totalSum);

}

    public OrderResponseDTO getOrderById(String orderId, AuthDetails currentUser) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with orderId: " + orderId));

        log.info("getOrderById: {}", orderId);
       OrderResponseDTO response = (order.getUserId().equals(currentUser.getCurrentUserId())
                    || currentUser.getRole().equals(Role.ADMIN))
                    ? mapToDTO(order) // normal mapping for client and admin
                    : mapToDTO(filterOrderForSeller(order, currentUser)); // seller gets only filtered items
        log.info("OrderResponseDTO prepared for orderId: {}", response.getTotalPrice());
        return response;
    }

    public OrderResponseDTO createOrder(OrderCreateDTO orderCreateDTO, AuthDetails currentUser) throws IOException {

        if (!currentUser.getRole().equals(Role.CLIENT)) {
            throw new BadRequestException("Current user is not a CLIENT and cannot place orders");
        }

        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId());
        if (cart == null || cart.getItems().isEmpty()) {
            throw new BadRequestException("Cart is empty. Cannot create order.");
        }

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

        existingOrder.setStatus(orderUpdate.getStatus());
        existingOrder.setUpdatedAt(new Date());

        // WHAT ELSE CAN BE UPDATED??

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
        orderRepository.delete(existingOrder);
    }

    // Helper methods

    // convert OrderItem to ItemDTO
    private ItemDTO toItemDTO(OrderItem item) {
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
        log.info("mapToDTO: fullName {}, street {}, postalCode {}, city {}, country {}",
                order.getShippingAddress().getFullName(),
                order.getShippingAddress().getStreet(),
                order.getShippingAddress().getPostalCode(),
                order.getShippingAddress().getCity(),
                order.getShippingAddress().getCountry()
        );
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

    // filter order items for a specific seller
    private Order filterOrderForSeller(Order order, AuthDetails currentUser) {
        if (!currentUser.getRole().equals(Role.SELLER)) {
            throw new ForbiddenException("Access denied to order with orderId: " + order.getId() + " for userId: " + currentUser.getCurrentUserId());
        }
        List<OrderItem> filteredItems = order.getItems().stream()
                .filter(item -> item.getSellerId().equals(currentUser.getCurrentUserId()))
                .toList();
        if (filteredItems.isEmpty()) {
            throw new ForbiddenException("Access denied to order with orderId: " + order.getId() + " for userId: " + currentUser.getCurrentUserId());
        }
        order.setItems(filteredItems);
        double sellerTotal = filteredItems.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
        order.setTotalPrice(sellerTotal);
        return order;
    }

}
