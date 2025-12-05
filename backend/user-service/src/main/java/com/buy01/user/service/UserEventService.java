package com.buy01.user.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserEventService {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topic.user-deleted}")
    private String userDeletedTopic;

    public void publishUserDeletedEvent(String userId) {
        System.out.println("Kafka called with user deleted event");
        kafkaTemplate.send(userDeletedTopic, userId);
    }
}
