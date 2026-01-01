package com.facebook.user.event;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class FeedEventListener {

    @KafkaListener(topics = "post-created-topic", groupId = "user-service-group")
    public void handlePostCreatedEvent(String eventJson) {
        // In a real app, we would deserialize this to a DTO
        log.info("User Service received PostCreatedEvent: {}", eventJson);

        // Logic to update user stats (e.g. increment post count) would go here
    }
}
