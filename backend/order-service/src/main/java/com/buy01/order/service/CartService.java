package com.buy01.order.service;

import com.buy01.order.dto.ProductUpdateDTO;
import com.buy01.order.exception.NotFoundException;
import com.buy01.order.model.Cart;
import com.buy01.order.model.CartStatus;
import com.buy01.order.model.OrderItem;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.security.AuthDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Service layer is responsible for business logic, validation, verification and data manipulation.
// It chooses how to handle data and interacts with the repository layer.
@Service
public class CartService {

    private final CartRepository cartRepository;

    @Autowired
    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public Cart getCurrentCart(String userId) {
        Cart cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new Cart();
            cart.setUserId(userId);
            cart.setItems(new ArrayList<>());
        }
        return cart;
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

        cartRepository.deleteItemByProductId(id);
    }

    public void deleteCart(AuthDetails currentUser) throws IOException {
        Cart cart = cartRepository.findByUserId(currentUser.getCurrentUserId());
        if (cart == null) {
            throw new NotFoundException("Cart not found");
        }
        cartRepository.deleteById(cart.getId());
    }

    // kafka logic for updating product info
    public void updateCartProducts(ProductUpdateDTO productUpdate) {
        List<Cart> carts = cartRepository.findByProductId(productUpdate.getProductId());

        for (Cart cart : carts) {
            if (cart.getCartStatus().equals(CartStatus.CHECKOUT)) {
                break;
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
}
