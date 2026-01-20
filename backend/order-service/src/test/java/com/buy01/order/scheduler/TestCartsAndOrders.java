package com.buy01.order.scheduler;

import com.buy01.order.model.*;
import com.buy01.order.security.AuthDetails;
import com.buy01.order.service.TestAuthFactory;

import java.util.Date;
import java.util.List;

public class TestCartsAndOrders {

    private TestCartsAndOrders() {}

    public static AuthDetails clientUser() {
        return new AuthDetails("user1", Role.CLIENT);
    }

    public static AuthDetails sellerUser() {
        return new AuthDetails("seller1", Role.SELLER);
    }

    public static AuthDetails sellerUser2() {
        return new AuthDetails("seller2", Role.SELLER);
    }

    public static AuthDetails adminUser() {
        return new AuthDetails("admin1", Role.ADMIN);
    }

    public static ShippingAddress shippingAddress() { return new ShippingAddress("test user", "street", "city", "12345", "Finland"); }


    static class TestOrder extends Order {
        TestOrder(String id, String userId, List<OrderItem> items, double totalPrice, OrderStatus status,
                  ShippingAddress shippingAddress) {
            super(id, userId, items, totalPrice, status, shippingAddress);
        }
    }

    static class TestCart extends Cart {
        TestCart(String id, String userId, List<OrderItem> items, double totalPrice, CartStatus cartStatus) {
            super(id, userId, items, totalPrice, cartStatus);
        }
    }

    public static TestOrder order1() {
        return new TestOrder(
                "order1",
                clientUser().getCurrentUserId(),
                List.of(
                        product1(),
                        product2()
                ),
                130.0,
                OrderStatus.DELIVERED,
                shippingAddress()
        );
    }

    public static TestOrder order2() {
        return new TestOrder(
                "order2",
                "user2",
                List.of(
                        product3()
                ),
                60.0,
                OrderStatus.SHIPPED,
                shippingAddress()
        );
    }

    public static TestCart cart1() {
        return new TestCart(
                "cart1",
                clientUser().getCurrentUserId(),
                List.of(
                        product1(),
                        product2()
                ),
                130.0,
                CartStatus.ACTIVE
        );
    }

    public static TestCart cart2() {
        return new TestCart(
                "cart2",
                clientUser().getCurrentUserId(),
                List.of(
                        product3()
                ),
                60.0,
                CartStatus.CHECKOUT
        );
    }

    public static TestCart cart3() {
        return new TestCart(
                "cart3",
                "user2",
                List.of(
                        product1(),
                        product3()
                ),
                150.0,
                CartStatus.ABANDONED
        );
    }

    public static OrderItem product1() {
        return new OrderItem("prod1", "Product 1", 2, 50.0, sellerUser().getCurrentUserId());
    }
    public static OrderItem product2() {
        return new OrderItem("prod2", "Product 2", 1, 30.0, sellerUser2().getCurrentUserId());
    }
    public static OrderItem product3() {
        return new OrderItem("prod3", "Product 3", 3, 20.0, sellerUser().getCurrentUserId());
    }


}
