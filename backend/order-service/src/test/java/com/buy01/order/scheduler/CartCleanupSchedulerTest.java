package com.buy01.order.scheduler;

import com.buy01.order.client.ProductClient;
import com.buy01.order.model.Cart;
import com.buy01.order.model.CartStatus;
import com.buy01.order.repository.CartRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import static com.buy01.order.scheduler.TestCartsAndOrders.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartCleanupSchedulerTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartCleanupScheduler scheduler;

    @Captor
    private ArgumentCaptor<Cart> cartCaptor;

    @Test
    @DisplayName("Test ACTIVE cart expiration to ABANDONED")
    void activeCartToAbandoned() {
        TestCart staleActiveCart = cart1();

        // Force updateTime to be 2 minutes ago
        Date twoMinutesAgo = new Date(System.currentTimeMillis() - (2 * 60 * 1000));
        ReflectionTestUtils.setField(staleActiveCart, "expiryTime", twoMinutesAgo);
        ReflectionTestUtils.setField(staleActiveCart, "cartStatus", CartStatus.ACTIVE);

        when(cartRepository.findExpiredActiveCarts(any(Date.class)))
                .thenReturn(List.of(staleActiveCart));
        when(cartRepository.findByCartStatus(CartStatus.CHECKOUT)).thenReturn(Collections.emptyList());
        when(cartRepository.findExpiredAbandonedCarts(any(Date.class))).thenReturn(Collections.emptyList());

        scheduler.processCartExpirations();

        verify(cartRepository).delete(cartCaptor.capture());
        verify(productClient).updateQuantity(product1().getProductId(), product1().getQuantity());
    }

    @Test
    @DisplayName("Test CHECKOUT cart expiration to ABANDONED")
    void checkoutToAbandoned() {
        Cart cart = cart2();

        // Set the updateTime to 6 minutes ago (to pass the fiveMinAgo check)
        Date sixMinutesAgo = new Date(System.currentTimeMillis() - (6 * 60 * 1000));
        ReflectionTestUtils.setField(cart, "updateTime", sixMinutesAgo);

        // Set the expiryTime to 1 minute ago (to trigger abandonment)
        Date oneMinuteAgo = new Date(System.currentTimeMillis() - (60 * 1000));
        ReflectionTestUtils.setField(cart, "expiryTime", oneMinuteAgo);

        when(cartRepository.findExpiredActiveCarts(any(Date.class))).thenReturn(Collections.emptyList());
        when(cartRepository.findByCartStatus(CartStatus.CHECKOUT)).thenReturn(List.of(cart));

        scheduler.processCartExpirations();

        verify(cartRepository).delete(any(Cart.class));
    }

    @Test
    @DisplayName("Test CHECKOUT cart reversion to ACTIVE if not expired")
    void checkoutToActive() {
        Cart checkoutCart = cart2();

        // Force updateTime to be 10 mins ago, so that scheduler processes it
        Date tenMinutesAgo = new Date(System.currentTimeMillis() - (10 * 60 * 1000));
        ReflectionTestUtils.setField(checkoutCart, "updateTime", tenMinutesAgo);

        // Force expiryTime to be 10 mins in the future, so that logic catches activateCart()
        Date tenMinutesFuture = new Date(System.currentTimeMillis() + (10 * 60 * 1000));
        ReflectionTestUtils.setField(checkoutCart, "expiryTime", tenMinutesFuture);

        when(cartRepository.findExpiredActiveCarts(any(Date.class))).thenReturn(Collections.emptyList());
        when(cartRepository.findByCartStatus(CartStatus.CHECKOUT)).thenReturn(List.of(checkoutCart));

        scheduler.processCartExpirations();

        verify(cartRepository).save(cartCaptor.capture());
        Cart savedCart = cartCaptor.getValue();
        assertEquals(CartStatus.ACTIVE, savedCart.getCartStatus());
    }

    @Test
    @DisplayName("Test ABANDONED cart deletion after expiration")
    void deleteAbandoned() {
        Cart abandonedCart = cart3();

        // UpdateTime to 2 minutes ago to match the 1-minute expiration logic
        Date twoMinutesAgo = new Date(System.currentTimeMillis() - (2 * 60 * 1000));
        org.springframework.test.util.ReflectionTestUtils.setField(abandonedCart, "updateTime", twoMinutesAgo);

        List<Cart> toDeleteList = List.of(abandonedCart);

        when(cartRepository.findExpiredActiveCarts(any(Date.class))).thenReturn(Collections.emptyList());
        when(cartRepository.findByCartStatus(CartStatus.CHECKOUT)).thenReturn(Collections.emptyList());
        when(cartRepository.findExpiredAbandonedCarts(any(Date.class))).thenReturn(toDeleteList);

        scheduler.processCartExpirations();

        verify(cartRepository).deleteAll(toDeleteList);
    }
}
