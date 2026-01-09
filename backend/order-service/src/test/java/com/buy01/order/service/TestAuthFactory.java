package com.buy01.order.service;

import com.buy01.order.model.Role;
import com.buy01.order.security.AuthDetails;

public class TestAuthFactory {

    private TestAuthFactory() {}

    public static AuthDetails clientUser() {
        return new AuthDetails("user1", Role.CLIENT);
    }

    public static AuthDetails sellerUser() {
        return new AuthDetails("seller1", Role.SELLER);
    }

    public static AuthDetails adminUser() {
        return new AuthDetails("admin1", Role.ADMIN);
    }

}
