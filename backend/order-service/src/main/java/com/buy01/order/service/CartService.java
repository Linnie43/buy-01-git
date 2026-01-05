package com.buy01.order.service;

import com.buy01.order.dto.ProductUpdateDTO;
import com.buy01.order.exception.NotFoundException;
import com.buy01.order.model.Cart;
import com.buy01.order.model.CartStatus;
import com.buy01.order.model.OrderItem;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.repository.OrderRepository;
import com.buy01.order.security.AuthDetails;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.buy01.order.client.ProductClient;
import com.buy01.order.model.Product;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Service layer is responsible for business logic, validation, verification and data manipulation.
// It chooses how to handle data and interacts with the repository layer.
@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    @Autowired
    public CartService(CartRepository cartRepository, ProductClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }

    /*public Cart getCurrentCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
        }
        return cart;
    }*/

    public Cart getActiveCart(String userId) {
        Cart cart = cartRepository.findByUserIdAndCartStatus(userId, CartStatus.ACTIVE); // find active cart for user
        if (cart == null) { // if no active cart
            cart = new Cart(userId, new ArrayList<>(), 0, CartStatus.ACTIVE); // create new cart
            cartRepository.save(cart); // save new cart to repository
        }
        return cart;
    }

    public Cart addToCart(String userId, String productId, int quantity) {
        if (quantity <= 0) throw new BadRequestException("Quantity must be > 0");

        ProductDTO product = productClient.getProductById(productId); // fetch product details from product service
        if (product == null) throw new NotFoundException("Product not found");

        OrderItem newItem = new OrderItem(productId, product.getName(), quantity, product.getPrice()); // create new order item
        Cart cart = getActiveCart(userId); // get or create active cart for user

        boolean exists = false;
        for (OrderItem item : cart.getItems()) { // iterate through existing items
            if (item.getProductId().equals(newItem.getProductId())) { // check if item already exists in cart
                item.setQuantity(item.getQuantity() + newItem.getQuantity()); // update quantity
                exists = true; // mark as existing
                break;
            }
        }

        if (!exists) { // if item does not exist in cart
            cart.getItems().add(newItem); // add new item to cart
        }

        cart.setTotalPrice(calculateTotal(cart.getItems()));  // renew total price
        cart.setUpdateTime(new java.util.Date());            // refresh update time
        return cartRepository.save(cart);                    // save and return updated cart
    }

    private double calculateTotal(List<OrderItem> items) { // calculate total price of items in cart
        return items.stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();
    }

    /*public void deleteItemById(String id, AuthDetails currentUser) {
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
    }*/

    /*public void deleteCart(AuthDetails currentUser) throws IOException {
        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId());
        if (cart == null) {
            throw new NotFoundException("Cart not found");
        }
        cartRepository.deleteById(cart.getId());
    }*/

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
