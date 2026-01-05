package com.buy01.product.client;

import com.buy01.product.dto.UserDTO;
import com.buy01.product.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient {

    private final RestTemplate restTemplate;
    private final String userServiceBaseUrl;

    public UserClient() {
        this.restTemplate = new RestTemplate();
        // Local dev URL; will switch to container name in Docker
        this.userServiceBaseUrl = "http://user-service:8080/api/users";
    }

    public String getRoleIfUserExists(String userId){
        try {
            UserDTO user = restTemplate.getForObject(
                    userServiceBaseUrl + "/internal/user/" + userId,
                    UserDTO.class
            );

            if (user == null) {
                throw new NotFoundException("User not found");
            }

            return user.getRole();
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("User not found");
        }
    }

}

