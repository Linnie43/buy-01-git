package com.buy01.order.service;

import com.buy01.order.dto.*;
import com.buy01.order.exception.ConflictException;
import com.buy01.order.exception.NotFoundException;
import com.buy01.order.model.Cart;
import com.buy01.order.model.CartStatus;
import com.buy01.order.model.OrderItem;
import com.buy01.order.model.Role;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.security.AuthDetails;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    public CartService(CartRepository cartRepository, ProductClient productClient, OrderService orderService) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
        this.orderService = orderService;
    }

    public Cart getCurrentCart(AuthDetails currentUser) throws BadRequestException {

        if (!currentUser.getRole().equals("CLIENT")) {
            throw new BadRequestException("Current user is not a CLIENT");
        }

        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId()); // find active cart for user
        if (cart == null) { // if no active cart
            cart = new Cart(currentUser.getCurrentUserId(), new ArrayList<>(), 0, CartStatus.ACTIVE); // create new cart
            cartRepository.save(cart); // save new cart to repository
        }

        return cart;
    }

    public CartResponseDTO addToCart(AuthDetails currentUser, CartItemRequestDTO newItem) throws IOException {

        if (!currentUser.getRole().equals(Role.CLIENT)) {
            throw new BadRequestException("Current user is not a CLIENT");
        }

        if (newItem.getQuantity() <= 0) throw new ConflictException("Product is out of stock");

        ProductUpdateDTO product = productClient.getProductById(newItem.getProductId()); // fetch product details from product service
        if (product == null) throw new NotFoundException("Product not found");

        OrderItem itemAdded = new OrderItem(product.getProductId(), product.getProductName(), newItem.getQuantity(), product.getProductPrice(), product.getSellerId()); // create new order item
        Cart cart = getCurrentCart(currentUser); // get or create active cart for user

        boolean exists = false;
        for (OrderItem item : cart.getItems()) { // iterate through existing items
            if (item.getProductId().equals(itemAdded.getProductId())) { // check if item already exists in cart
                item.setQuantity(item.getQuantity() + itemAdded.getQuantity()); // update quantity
                exists = true; // mark as existing
                break;
            }
        }

        if (!exists) { // if item does not exist in cart
            cart.getItems().add(itemAdded); // add new item to cart
        }

        cart.setTotalPrice(calculateTotal(cart.getItems()));  // renew total price
        cart.setUpdateTime(new Date());            // refresh update time
        return mapToDTO(cartRepository.save(cart));  // save and return updated cart

    }

    private double calculateTotal(List<OrderItem> items) { // calculate total price of items in cart
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    public void deleteItemById(String id, AuthDetails currentUser) {
        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId());
        if (cart == null) {
            throw new NotFoundException("Cart not found");
        }
        Optional<OrderItem> item = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(id))
                .findFirst();

        if (item.isEmpty()) {
            throw new NotFoundException("Item not found");
        }

        cart.getItems().removeIf(i -> i.getProductId().equals(id));
        cart.setTotalPrice(calculateTotal(cart.getItems()));
        cartRepository.save(cart);
    }

    public void deleteCart(AuthDetails currentUser) throws IOException {
        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId());
        if (cart == null) {
            throw new NotFoundException("Cart not found");
        }
        cartRepository.deleteById(cart.getId());
    }

    public CartResponseDTO mapToDTO(Cart cart) {

        return new CartResponseDTO(
                cart.getId(),
                cart.getItems().stream()
                        .map(orderService::toItemDTO)
                        .toList(),
                calculateTotal(cart.getItems())
        );
    }

    // kafka logic for updating product info when product details change (in all carts containing the product)
    /*public void updateCartProducts(ProductUpdateDTO productUpdate) {
        List<Cart> carts = cartRepository.findByProductId(productUpdate.getProductId());

        for (Cart cart : carts) {
            if (cart.getCartStatus().equals(CartStatus.CHECKOUT)) {
                break; // or continue?
            }

            for (OrderItem orderItem : cart.getItems()) {
                if (orderItem.getProductId().equals(productUpdate.getProductId())) {
                    if (productUpdate.getProductName() != null) {
                        orderItem.setProductName(productUpdate.getProductName());
                    }
                    if (productUpdate.getProductPrice() != null) {
                        orderItem.setPrice(productUpdate.getProductPrice());
                    }
                    break;
                }
            }
        }

        cartRepository.saveAll(carts);

    }

    //kafka logic for deleting products that are no longer available

    //fetch products from product service (needs productClient)

     */
}
