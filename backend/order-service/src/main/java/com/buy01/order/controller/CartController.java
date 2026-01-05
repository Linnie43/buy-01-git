package com.buy01.order.controller;

import com.buy01.order.dto.CartItemRequestDTO;
import com.buy01.order.dto.CartItemUpdateDTO;
import com.buy01.order.dto.CartResponseDTO;
import com.buy01.order.dto.ItemDTO;
import com.buy01.order.model.Cart;
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
    public ResponseEntity<?> addToCart(
            @RequestHeader("Authorization") String authHeader,
            @Valid @ModelAttribute CartItemRequestDTO newItem) throws IOException {

        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        if (!currentUser.getRole().equals("CLIENT")) {
            throw new BadRequestException("Current user is not a CLIENT");
        }

        // CART LOGIC
        // fetch cart for userId and create one or add item to it
        // product details fetched from product service, not trusting client information

        return ResponseEntity.ok("new item added to cart");
    }

    @GetMapping
    public ResponseEntity<?> getCurrentCart(
            @RequestHeader("Authorization") String authHeader
            ) throws BadRequestException {
        AuthDetails currentUser = securityUtils.getAuthDetails(authHeader);

        if (!currentUser.getRole().equals("CLIENT")) {
            throw new BadRequestException("Current user is not a CLIENT");
        }

        Cart cart = cartService.getCurrentCart(currentUser.getCurrentUserId());
        List<ItemDTO> itemsDto = cart.getItems().stream()
                .map(item -> new ItemDTO(
                        item.getProductId(),
                        item.getProductName(),
                        item.getQuantity(),
                        item.getPrice(),
                        item.getPrice() * item.getQuantity()
                ))
                .toList();

        return ResponseEntity.ok(new CartResponseDTO(
                cart.getId(),
                itemsDto,
                itemsDto.stream().mapToDouble(ItemDTO::getPrice).sum()
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
