package com.buy01.order.controller;

import com.buy01.order.dto.CartItemRequestDTO;
import com.buy01.order.dto.CartItemUpdateDTO;
import com.buy01.order.dto.CartResponseDTO;
import com.buy01.order.dto.ItemDTO;
import com.buy01.order.model.Cart;
import com.buy01.order.model.Role;
import com.buy01.order.security.AuthDetails;
import com.buy01.order.security.SecurityUtils;
import com.buy01.order.service.CartService;
import jakarta.validation.Valid;
import org.apache.coyote.BadRequestException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final SecurityUtils securityUtils;

    public CartController(CartService cartService, SecurityUtils securityUtils) {
        this.cartService = cartService;
        this.securityUtils = securityUtils;
    }

    @PostMapping
    public ResponseEntity<CartResponseDTO> addToCart(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody CartItemRequestDTO newItem) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        return ResponseEntity.ok(cartService.addToCart(
                currentUser,
                newItem
                ));
    }

    @GetMapping
    public ResponseEntity<CartResponseDTO> getCurrentCart(
            @RequestHeader("Authorization") String authHeader
            ) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        return ResponseEntity.ok(
                cartService.mapToDTO(
                        cartService.getCurrentCart(currentUser)
                ));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<?> updateCart(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String productId,
            @Valid @ModelAttribute CartItemUpdateDTO itemUpdate) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        //LOGIC FOR UPDATING AMOUNT FOR ONE ITEM

        return ResponseEntity.ok("quantity updated");
    }


    @DeleteMapping("/{productId}")
    public ResponseEntity<?> deleteProduct(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String productId
    ) {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        cartService.deleteItemById(productId, currentUser);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/all")
    public ResponseEntity<?> deleteCart(@RequestHeader("Authorization") String authHeader) throws IOException {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        cartService.deleteCart(currentUser);
        return ResponseEntity.ok().build();
    }
}
