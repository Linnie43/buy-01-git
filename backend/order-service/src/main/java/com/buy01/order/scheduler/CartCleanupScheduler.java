package com.buy01.order.scheduler;

import com.buy01.order.client.ProductClient;
import com.buy01.order.model.Cart;
import com.buy01.order.model.CartStatus;
import com.buy01.order.model.OrderItem;
import com.buy01.order.repository.CartRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CartCleanupScheduler {

    private final CartRepository cartRepository;
    private final ProductClient productClient;

    // Run every 30 seconds to catch the 1-minute and 15-minute windows
    @Scheduled(fixedRate = 30000)
    public void processCartExpirations() {
        Date now =  new Date();
        Date oneMinAgo = new Date(now.getTime() - (60 * 1000));
        Date fiveMinAgo = new Date(now.getTime() - (5 * 60 * 1000));

        // Mark ACTIVE Carts started 15min ago to ABANDONED
        List<Cart> toAbandon = cartRepository.findExpiredActiveCarts(now);
        for (Cart cart : toAbandon) {
            if (cart.getCartStatus() == CartStatus.ACTIVE && cart.getUpdateTime().before(oneMinAgo)) { // avoid double processing and give grace
                abandonCart(cart);
            }
        }

        // Mark CHECKOUT Carts to ABANDONED (The 5-minute expiry logic)
        List<Cart> checkoutCarts = cartRepository.findByCartStatus(CartStatus.CHECKOUT);
        for (Cart cart : checkoutCarts) {
            // Logic: if updateTime > 5 min ago AND expiryTime (if exists) is past now
            if (cart.getUpdateTime().before(fiveMinAgo)) {
                if (cart.getExpiryTime() != null && cart.getExpiryTime().before(now)) {
                    abandonCart(cart);
                } else {
                    activateCart(cart);
                }
            }
        }

        // Delete Carts with status ABANDONED for more than 1 min
        List<Cart> toDelete = cartRepository.findExpiredAbandonedCarts(oneMinAgo);
        if (!toDelete.isEmpty()) {
            cartRepository.deleteAll(toDelete);
        }
    }

    private void abandonCart(Cart cart) {
        cart.setCartStatus(CartStatus.ABANDONED);
        cart.setUpdateTime(new Date());
        cartRepository.save(cart);

        for(OrderItem item : cart.getItems()) {
            productClient.updateQuantity(item.getProductId(), item.getQuantity());
        }
    }

    private void activateCart(Cart cart) {
        cart.setCartStatus(CartStatus.ACTIVE);
        cart.setUpdateTime(new Date());
        cartRepository.save(cart);
    }
}
