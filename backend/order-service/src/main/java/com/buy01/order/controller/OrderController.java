package com.buy01.order.controller;

import com.buy01.order.model.Order;
import com.buy01.order.security.AuthDetails;
import com.buy01.order.service.OrderService;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import com.buy01.order.dto.OrderResponseDTO;
import com.buy01.order.security.SecurityUtils;
import com.buy01.order.dto.OrderUpdateRequest;
import com.buy01.order.dto.OrderCreateDTO;
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
    public ResponseEntity<?> createOrder(
            @RequestHeader("Authorization") String authHeader,
            @Valid @ModelAttribute OrderCreateDTO request) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);


        if (!currentUser.getRole().equals("CLIENT")) {
            throw new BadRequestException("Current user is not a CLIENT");
        }

        // ORDER CREATION LOGIC

        return ResponseEntity.ok("new order created");
    }

    // get all orders for the current user (client or seller)
    @GetMapping
    public List<OrderResponseDTO> getOwnOrders(
            @RequestHeader("Authorization") String authHeader
            ) {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        // GET ALL ORDERS FOR THE CURRENT USER

        // SELLER ORDERS OR CLIENT ORDERS

        return null;
    }


    // get a specific order details
    @GetMapping("/{id}")
    public ResponseEntity<?> getOrderById(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id) {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        //Order order = orderService.getOrderById(id);

        OrderResponseDTO o = new OrderResponseDTO(
//                order.getId(),
//                order.getUserId(),
//                order.getItems(),
//                order.getTotalPrice(),
//                order.getStatus(),
//                order.getCreatedAt(),
//                order.getUpdatedAt()
        );

        return ResponseEntity.ok(o);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateOrder(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id,
            @Valid @ModelAttribute OrderUpdateRequest request) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        //OrderResponseDTO updated = orderService.updateOrder(id, request, currentUser);

        return ResponseEntity.ok("updated");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String id
    ) {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        //orderService.deleteOrder(id, currentUser);

        return ResponseEntity.ok().build();
    }
}
