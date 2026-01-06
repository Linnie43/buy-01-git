package com.buy01.order.controller;

import com.buy01.order.dto.OrderCreateDTO;
import com.buy01.order.model.Role;
import com.buy01.order.security.AuthDetails;
import com.buy01.order.service.OrderService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import com.buy01.order.dto.OrderResponseDTO;
import com.buy01.order.security.SecurityUtils;
import com.buy01.order.dto.OrderUpdateRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final SecurityUtils securityUtils;

    public OrderController(OrderService orderService, SecurityUtils securityUtils) {
        this.orderService = orderService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<OrderResponseDTO> createOrder(
            @RequestHeader("Authorization") String authHeader,
            @Valid @ModelAttribute OrderCreateDTO orderDto) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);
        return ResponseEntity.ok(orderService.createOrder(orderDto, currentUser));
    }

    // get all orders for the current user (client or seller)
    @GetMapping
    public ResponseEntity<List<OrderResponseDTO>> getOwnOrders(
            @RequestHeader("Authorization") String authHeader
            ) throws IOException {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);
        List<OrderResponseDTO> orders;

        if (currentUser.getRole().equals(Role.CLIENT)) {
            orders = orderService.getClientOrders(currentUser);
        } else if (currentUser.getRole().equals(Role.SELLER)) {
            orders = orderService.getSellerOrders(currentUser);
        } else {
            throw new BadRequestException("Invalid role" + currentUser.getRole().toString() + " for fetching own orders");
        }

        return ResponseEntity.ok(orders);
    }


    // get a specific order details
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> getOrderById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String orderId) {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        return ResponseEntity.ok(orderService.getOrderById(orderId, currentUser));
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<OrderResponseDTO> updateOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String orderId,
            @Valid @ModelAttribute OrderUpdateRequest request) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        return ResponseEntity.ok(orderService.updateOrder(orderId, request, currentUser));
    }


    // Client can delete their own orders with status "CREATED", ADMIN can delete any order
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String orderId
    ) {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        orderService.deleteOrderById(orderId, currentUser);

        return ResponseEntity.ok().build();
    }
}
