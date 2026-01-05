package com.buy01.user.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import com.buy01.user.model.Role;

import java.util.Date;

@Document(collection = "users")
public class User {
    @Id
    private String userId;
    private String name;
    private String email;
    private String password;
    private Role role;
    private String avatarUrl;
    private Date createTime;
    private Date updateTime;

    // constructor
    public User() {}
    public User(String userId, String name, String email, String password, Role role,  String avatarUrl) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.createTime = new Date();
        this.updateTime = new Date();
    }

    // getters and setters
    public String getId() { return userId; } //MongoDb generates id automatically
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public Role getRole() { return role; }
    public String getAvatarUrl() { return avatarUrl; }
    public Date getCreateTime() { return createTime; }
    public Date getUpdateTime() { return updateTime; }

    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(Role role) { this.role = role; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setCreateTime(Date createTime) { this.createTime = createTime; }
    public void setUpdateTime(Date updateTime) { this.updateTime = updateTime; }
}
