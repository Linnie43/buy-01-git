package com.buy01;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.core.MongoTemplate;

@SpringBootApplication
public class Buy01Application {

    @Autowired
    private MongoTemplate mongoTemplate;

    public static void main(String[] args) {
		SpringApplication.run(Buy01Application.class, args);
	}

    @PostConstruct
    public void checkDb() {
        System.out.println("Current DB: " + mongoTemplate.getDb().getName());
    }

}
