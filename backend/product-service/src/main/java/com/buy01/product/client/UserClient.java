package com.buy01.product.client;

import com.buy01.product.dto.UserDTO;
import com.buy01.product.exception.NotFoundException;
import com.buy01.product.model.Role;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Service
public class UserClient {

    private final RestTemplate restTemplate;
    private static final String USER_SERVICE_BASE_URL = "http://user-service:8080/api/users";

    public UserClient() {
        this.restTemplate = new RestTemplate();
    }

    public Role getRoleIfUserExists(String userId){
        try {
            UserDTO user = restTemplate.getForObject(
                    USER_SERVICE_BASE_URL + "/internal/user/" + userId,
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

