package com.buy01.user.dto;

public class UserUpdateRequest {
    private String name;
    private String email;
    private String password; // the admin can only change the password
    // cannot update role after registration

    public UserUpdateRequest() {}
    public UserUpdateRequest(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

}
