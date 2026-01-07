package com.buy01.order.dto;

public class ProductUpdateDTO {
    private String productId;
    private String productName;
    private Double productPrice;
    private int quantity;
    private String sellerId;

    public ProductUpdateDTO() {}
    public ProductUpdateDTO(String productId, String productName, Double productPrice, int quantity, String sellerId) {
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

    public Double getProductPrice() {return productPrice;}
    public void setProductPrice(Double productPrice) {this.productPrice = productPrice;}

    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}

    public String getSellerId() {return sellerId;}
    public void setSellerId(String sellerId) {this.sellerId = sellerId;}
}
