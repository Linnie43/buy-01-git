package com.buy01.order.exception;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException(String message) {
        super(message);
    }
    public OutOfStockException(String message, Throwable cause) {}
}
