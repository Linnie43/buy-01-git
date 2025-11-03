package com.buy01.media.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document (collection = "media")
public class Media {
    @Id
    private String id;
    private String path;
    private String productId;

    public Media() {}
    public Media(String path, String productId) {
        this.path = path;
        this.productId = productId;
    }

    public String getId() { return id; } // MongoDB generates ID, no setter for it
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}
