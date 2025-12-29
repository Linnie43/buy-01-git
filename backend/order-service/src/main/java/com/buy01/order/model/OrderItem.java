package com.buy01.order.model;

public class OrderItem {

    private String productId;
    private String productName;
    private int  quantity;
    private double price;

    //Constructor
    public OrderItem(String productId, String productName, int quantity, double price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    public OrderItem() {}

    public String getProductId() {return this.productId;}
    public void setProductId(String productId) {this.productId = productId;}

    public String getProductName() {return this.productName;}
    public void setProductName(String productName) {this.productName = productName;}

    public int getQuantity() {return this.quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}

    public double getPrice() {return this.price;}
    public void setPrice(double price) {this.price = price;}

}
