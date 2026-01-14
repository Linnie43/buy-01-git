package com.buy01.order.service;

import com.buy01.order.dto.*;
import com.buy01.order.exception.ForbiddenException;
import com.buy01.order.exception.NotFoundException;
import com.buy01.order.exception.OutOfStockException;
import com.buy01.order.exception.BadRequestException;
import com.buy01.order.model.*;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.repository.OrderRepository;
import com.buy01.order.security.AuthDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.buy01.order.client.ProductClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

// Service layer is responsible for business logic, validation, verification and data manipulation.
// It chooses how to handle data and interacts with the repository layer.
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private static final Logger log = LoggerFactory.getLogger(CartService.class);


    @Autowired
    public CartService(CartRepository cartRepository, ProductClient productClient, OrderService orderService, OrderRepository orderRepository) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    public Cart getCurrentCart(AuthDetails currentUser) throws IOException {

        if (!currentUser.getRole().equals(Role.CLIENT)) {
            throw new BadRequestException("Current user is not a CLIENT");
        }

        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId()); // find active cart for user
        if (cart == null) { // if no active cart
            return null;
        }

        if (cart.getCartStatus() == CartStatus.ABANDONED) {
            reactivateCart(cart.getId());
        }

        return cart;
    }

    public Cart createNewCart(AuthDetails currentUser) {
        Cart cart = new Cart(
                currentUser.getCurrentUserId(),
                new ArrayList<>(),
                0.0,
                CartStatus.ACTIVE
        );
        return cartRepository.save(cart);
    }

    public CartResponseDTO addToCart(AuthDetails currentUser, CartItemRequestDTO newItem) throws IOException {

        if (!currentUser.getRole().equals(Role.CLIENT)) {
            throw new BadRequestException("Current user is not a CLIENT");
        }

        Cart cart = getCurrentCart(currentUser); // get or create active cart for user
        if (cart == null) {
            cart = createNewCart(currentUser);
        }
        validStatusForChanges(cart);

        ProductUpdateDTO product = productClient.getProductById(newItem.getProductId()); // fetch product details from product service
        if (product == null) throw new NotFoundException("Product not found");
        if (product.getQuantity() <= 0) throw new OutOfStockException("Product is out of stock");

        log.info("newItem ID {}, sellerId {}", newItem.getProductId(), product.getSellerId());
        OrderItem itemAdded = new OrderItem(product.getProductId(), product.getProductName(), newItem.getQuantity(), product.getProductPrice(), product.getSellerId()); // create new order item

        addOrUpdateItemInCart(cart, itemAdded); // add or update item in cart
        updateCartTotalAndTime(cart);

        return mapToDTO(cartRepository.save(cart));  // save and return updated cart

    }

    public CartResponseDTO addToCartFromOrder(AuthDetails currentUser, String orderId) throws IOException {
        if (!currentUser.getRole().equals(Role.CLIENT)) {
            throw new BadRequestException("Current user is not a CLIENT");
        }

        Order order = orderRepository.getOrderById(orderId)
                .orElseThrow(() -> new NotFoundException("Order not found with orderId: " + orderId));

        if (!order.getUserId().equals(currentUser.getCurrentUserId()) ) {
            throw new ForbiddenException("Order does not belong to the current userId " + currentUser.getCurrentUserId());
        }

        Cart cart = getCurrentCart(currentUser);
        if (cart == null) {
            cart = createNewCart(currentUser);
        }

        if (cart.getCartStatus() == CartStatus.ABANDONED) {
            reactivateCart(cart.getId());
        }
        validStatusForChanges(cart);

        for (OrderItem itemAdded : order.getItems()) {
            ProductUpdateDTO product = productClient.getProductById(itemAdded.getProductId());
            if (product == null) break;
            if (product.getQuantity() < itemAdded.getQuantity()) {
                itemAdded.setQuantity(product.getQuantity());
            }
            addOrUpdateItemInCart(cart, itemAdded);
        }

        updateCartTotalAndTime(cart);

        return mapToDTO(cartRepository.save(cart));  // save and return updated cart
    }

    public CartResponseDTO updateCart(AuthDetails currentUser, String productId, CartItemUpdateDTO newQuantity) throws IOException {
        Cart cart = getCurrentCart(currentUser);
        if (cart == null) {
            throw new NotFoundException("Cart not found");
        }
        validStatusForChanges(cart);

        Optional<OrderItem> itemToUpdate = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(productId)).findFirst();

        if (itemToUpdate.isEmpty()) {
            throw new NotFoundException("Item not found in cart");
        }

        int quantityDifference = newQuantity.getQuantity() - itemToUpdate.get().getQuantity();

        // update product quantity in product service
        productClient.updateQuantity(itemToUpdate.get().getProductId(), -quantityDifference);

        itemToUpdate.get().setQuantity(newQuantity.getQuantity());
        updateCartTotalAndTime(cart);

        return mapToDTO(cartRepository.save(cart));
    }

    public CartResponseDTO updateCartStatus(AuthDetails currentUser, CartStatus newStatus) throws IOException {
        Cart cart = getCurrentCart(currentUser);
        if (cart == null) {
            throw new NotFoundException("Cart not found");
        }

        if (cart.getCartStatus() == CartStatus.ABANDONED && newStatus != CartStatus.ABANDONED) {
            reactivateCart(cart.getId());
        }

        cart.setCartStatus(newStatus);
        updateCartTotalAndTime(cart);

        return mapToDTO(cartRepository.save(cart));
    }

    @PreAuthorize("hasRole('CLIENT') || hasRole('ADMIN')")
    public CartResponseDTO deleteItemById(String id, AuthDetails currentUser) {

        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId());
        if (cart == null) throw new NotFoundException("Cart not found");

        validStatusForChanges(cart);

        OrderItem itemToRemove = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Item not found in cart"));


        int quantity = itemToRemove.getQuantity();

        cart.getItems().remove(itemToRemove);

        // return item to the stock in product service if it was reserved (ACTIVE cart)
        if (cart.getCartStatus() == CartStatus.ACTIVE) {
            productClient.updateQuantity(id, quantity);
        }

        log.info("cart items {}", cart.getItems().size());

        if (cart.getItems().isEmpty()) {
            cartRepository.delete(cart);
            return null;
        } else {
            updateCartTotalAndTime(cart);
            return mapToDTO(cartRepository.save(cart));
        }
    }

    public void deleteCart(AuthDetails currentUser) {
        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId());
        if (cart == null) {
            throw new NotFoundException("Cart not found");
        }
        if (cart.getCartStatus() == CartStatus.ACTIVE) {
            for (OrderItem item : cart.getItems()) {
                productClient.updateQuantity(item.getProductId(), item.getQuantity());
            }
        }
        cartRepository.deleteById(cart.getId());
    }


    // Helper methods

    public void reactivateCart(String cartId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow();

        if (cart.getCartStatus() == CartStatus.ABANDONED) {
           try {
               for (OrderItem orderItem : cart.getItems()) {
                   ProductUpdateDTO product = productClient.getProductById(orderItem.getProductId());
                   if (product.getQuantity() < orderItem.getQuantity()) {
                       throw new OutOfStockException("Product " + product.getProductName() + " is out of stock.");
                   } else {
                       productClient.updateQuantity(orderItem.getProductId(), -orderItem.getQuantity());
                   }
               }
                cart.setCartStatus(CartStatus.ACTIVE);
                cart.setUpdateTime(new Date());
           } catch (Exception e) {
               throw new OutOfStockException("Some items are no longer available.", e);
           }

        }

    }

    // add new item to cart if it isn't there yet or update quantity for existing item
    private void addOrUpdateItemInCart(Cart cart, OrderItem itemAdded) {
        boolean exists = false;
        for (OrderItem item : cart.getItems()) { // iterate through existing items
            if (item.getProductId().equals(itemAdded.getProductId())) { // check if item already exists in cart
                item.setQuantity(item.getQuantity() + itemAdded.getQuantity()); // update quantity
                exists = true;
                break;
            }
        }

        if (!exists) { // if item does not exist in cart
            cart.getItems().add(itemAdded); // add new item to cart
        }

        productClient.updateQuantity(itemAdded.getProductId(), -itemAdded.getQuantity());
    }

    // update total price and update time of cart
    private void updateCartTotalAndTime(Cart cart) {
        cart.setTotalPrice(calculateTotal(cart.getItems()));
        cart.setUpdateTime(new Date());
    }

    // calculate total price of items in cart
    private double calculateTotal(List<OrderItem> items) {
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    // mapping Cart to CartResponseDTO
    public CartResponseDTO mapToDTO(Cart cart) {
        Date expiry = cart.getExpiryTime();
        if (expiry == null) {
            expiry = new Date(cart.getCreateTime().getTime() + (15 * 60 * 1000)); // 15 minutes from creation time
        }

        return new CartResponseDTO(
                cart.getId(),
                cart.getItems().stream()
                        .map(orderService::toItemDTO)
                        .toList(),
                calculateTotal(cart.getItems()),
                expiry
        );
    }

    // kafka logic for updating product info
    public void updateCartProducts(ProductUpdateDTO productUpdate) {
        List<Cart> carts = cartRepository.findByItemsProductId(productUpdate.getProductId());

        for (Cart cart : carts) {
            if (cart.getCartStatus().equals(CartStatus.CHECKOUT)) {
                break;
            }

            for (OrderItem orderItem : cart.getItems()) {
                if (orderItem.getProductId().equals(productUpdate.getProductId())) {
                    if (productUpdate.getProductName() != null) {
                        orderItem.setProductName(productUpdate.getProductName());
                    }
                    orderItem.setPrice(productUpdate.getProductPrice());

                    break;
                }
            }
        }

        cartRepository.saveAll(carts);

    }

    public void validStatusForChanges(Cart cart) {
        if (cart.getCartStatus().equals(CartStatus.CHECKOUT)) {
            throw new BadRequestException("Cannot update a CHECKOUT cart");
        }

    }

    //kafka logic for deleting products that are no longer available
}
