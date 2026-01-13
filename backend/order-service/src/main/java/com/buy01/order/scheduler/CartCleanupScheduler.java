package com.buy01.order.scheduler;

import com.buy01.order.model.Cart;
import com.buy01.order.model.CartStatus;
import com.buy01.order.repository.CartRepository;
import com.buy01.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CartCleanupScheduler {

    private final CartRepository cartRepository;
    private final OrderService orderService;

    // Run every 30 seconds to catch the 1-minute and 15-minute windows
    @Scheduled(fixedRate = 30000)
    public void processCartExpirations() {
        Date now =  new Date();
        Date fifteenMinsAgo = new Date(now.getTime() - (15 * 60 * 1000));
        Date oneMinAgo = new Date(now.getTime() - (60 * 1000));

        // Mark ACTIVE Carts started 15min ago to ABANDONED
        List<Cart> toAbandon = cartRepository.findExpiredActiveCarts(fifteenMinsAgo);
        for (Cart cart : toAbandon) {
            if (cart.getCartStatus() == CartStatus.ACTIVE && cart.getUpdateTime().before(oneMinAgo)) { // avoid double processing and give grace
                abandonCart(cart);
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

        orderService.restoreProductStock(cart.getItems());

    }
}
