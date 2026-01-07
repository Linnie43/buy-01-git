package com.buy01.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductUpdateDTO {
    private String productId;
    @JsonProperty("name")
    private String productName;
    @JsonProperty("price")
    private double productPrice;
    private int quantity;
    @JsonProperty("userId")
    private String sellerId;

    public ProductUpdateDTO() {}
    public ProductUpdateDTO(String productId, String productName, double productPrice, int quantity, String sellerId) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
        this.sellerId = sellerId;
    }

    public String getProductId() {return productId;}
    public void setProductId(String productId) {this.productId = productId;}

    public String getProductName() {return productName;}
    public void setProductName(String productName) {this.productName = productName;}

    public double getProductPrice() {return productPrice;}
    public void setProductPrice(double productPrice) {this.productPrice = productPrice;}

    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}

    public String getSellerId() {return sellerId;}
    public void setSellerId(String sellerId) {this.sellerId = sellerId;}
}
