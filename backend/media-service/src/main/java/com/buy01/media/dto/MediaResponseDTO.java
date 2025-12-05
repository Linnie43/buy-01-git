package com.buy01.media.dto;

public class MediaResponseDTO {
    private String id;
    private String productId;

    public MediaResponseDTO() {}
    public MediaResponseDTO(String id, String productId) {
        this.id = id;
        this.productId = productId;
    }

    public String getId() {return id;}
    public void setId(String id) { this.id = id; }

    public String getProductId() { return this.productId; }
    public void setProductId(String productId) { this.productId = productId; }

}
