package com.buy01.order.model;

import java.util.Collections;
import java.util.Set;

public enum OrderStatus {
    CREATED,
    CONFIRMED,
    SHIPPED,
    DELIVERED,
    CANCELLED;

    private Set<OrderStatus> allowedNext;

    static {
        CREATED.allowedNext = Set.of(CONFIRMED, CANCELLED);
        CONFIRMED.allowedNext = Set.of(SHIPPED, CANCELLED);
        SHIPPED.allowedNext = Set.of(DELIVERED);
        DELIVERED.allowedNext = Set.of();
        CANCELLED.allowedNext = Set.of();
    }

    public Set<OrderStatus> getAllowedNext() {
        return allowedNext == null ? Collections.emptySet() : allowedNext;
    }

    public boolean canTransitionTo(OrderStatus next) {
        return allowedNext.contains(next);

    }

    public OrderStatus getNextActiveStatus() {
        return getAllowedNext().stream()
                .filter(status -> status != CANCELLED)
                .findFirst()
                .orElse(null); // Return null if no next active status exists (e.g., DELIVERED)
    }
}

