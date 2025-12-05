package com.buy01.media.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document (collection = "media")
public class Media {
    @Id
    private String id;
    private String name;
    private String path;
    private String productId;

    public Media() {}
    public Media(String name, String path, String productId) {
        this.name = name;
        this.path = path;
        this.productId = productId;
    }

    // Testing purpose constructor
    protected Media(String id, String name, String path, String productId) {
        this.id = id;
        this.name = name;
        this.path = path;
        this.productId = productId;
    }

    public String getId() { return id; } // MongoDB generates ID, no setter for it
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
}
