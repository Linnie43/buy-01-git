package com.buy01.dto;

public class UserUpdateDTO {
    private String name;
    private String email;
    private String password; // the admin can only change the password
    private String role;     // the admin can change the role of the user

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
