package com.buy01.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import org.springframework.data.mongodb.core.index.Indexed;
import com.buy01.user.model.Role;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.web.multipart.MultipartFile;


// DTO for user signup requests with validation annotations
public class UserCreateDTO {

    @Indexed(unique = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email is invalid")
    @Pattern(
            regexp = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$",
            message = "Email must be a valid format (e.g. name@example.com)"
    )
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 3, message = "Password must be at least 3 characters")
    private String password;

    @NotBlank(message = "First ame is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 20 characters")
    private String firstname;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 20 characters")
    private String lastname;

    @Field(targetType = FieldType.STRING)
    private Role role;

    private MultipartFile avatar;

    public UserCreateDTO() {}
    public UserCreateDTO(String firstname, String lastname, String email, String password, Role role, MultipartFile avatar) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.password = password;
        this.role = role;
        this.avatar = avatar;
    }

    public String getFirstname() { return firstname; }
    public void setFirstname(String firstname) { this.firstname = firstname; }

    public String getLastname() { return lastname; }
    public void setLastname(String lastname) { this.lastname = lastname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public MultipartFile getAvatar() { return avatar; }
    public void setAvatar(MultipartFile avatar) { this.avatar = avatar;}

}



