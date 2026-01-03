package com.facebook.notification.service;

import com.facebook.notification.dto.PasswordResetEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetConsumerService {

    private final EmailService emailService;

    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @KafkaListener(topics = "password-reset-topic", groupId = "notification-group")
    public void consumePasswordResetEvent(String eventJson) {
        try {
            PasswordResetEvent event = objectMapper.readValue(eventJson, PasswordResetEvent.class);
            log.info("Received password reset event for email: {}", event.email());
            emailService.sendPasswordResetEmail(event.email(), event.username(), event.resetToken());
        } catch (Exception e) {
            log.error("Failed to process password reset event", e);
        }
    }
}
