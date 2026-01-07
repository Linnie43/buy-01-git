package com.buy01.user.config;

import com.buy01.user.model.Role;
import com.buy01.user.model.User;
import com.buy01.user.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;

    public DataInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String adminEmail = "admin@admin.com";
        int retries = 10;
        int wait = 5000; // 5 seconds

        while (retries > 0) {
            try {
                if (userRepository.findByEmail(adminEmail).isEmpty()) {
                    User admin = new User();
                    admin.setName("Admin");
                    admin.setEmail(adminEmail);
                    admin.setPassword(new BCryptPasswordEncoder().encode("password"));
                    admin.setRole(Role.ADMIN);
                    userRepository.save(admin);
                    System.out.println("Default admin created");
                }
                break; // success, exit loop
            } catch (Exception e) {
                retries--;
                System.out.println("Mongo not ready, retrying in " + wait / 1000 + "s... (" + retries + " retries left)");
                Thread.sleep(wait);
            }
        }

        if (retries == 0) {
            throw new IllegalStateException("MongoDB not ready after multiple attempts");
        }
    }
}

