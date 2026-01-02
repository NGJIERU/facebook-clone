package com.facebook.notification.service;

import com.facebook.notification.dto.PostCreatedEvent;
import com.facebook.notification.model.Notification;
import com.facebook.notification.model.NotificationType;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "post-created-topic", groupId = "notification-group")
    public void consumePostCreated(String eventJson) {
        try {
            log.info("Received Kafka event: {}", eventJson);
            PostCreatedEvent event = objectMapper.readValue(eventJson, PostCreatedEvent.class);

            // For now, notify the author themselves that their post is live.
            // In a real app, we would query followers and notify them.
            Notification notification = Notification.builder()
                    .recipientId(event.getAuthorId())
                    .senderId("SYSTEM")
                    .type(NotificationType.POST_CREATED)
                    .message("Your post is live: " + event.getContentSnippet())
                    .resourceId(event.getPostId())
                    .isRead(false)
                    .build();

            notificationService.saveAndSend(notification);

        } catch (Exception e) {
            log.error("Error processing Kafka event", e);
        }
    }

    @KafkaListener(topics = "notification-topic", groupId = "notification-group")
    public void consumeNotification(String eventJson) {
        try {
            log.info("Received notification event: {}", eventJson);
            var node = objectMapper.readTree(eventJson);
            
            String type = node.get("type").asText();
            String recipientId = node.get("recipientId").asText();
            String senderId = node.get("senderId").asText();
            String message = node.get("message").asText();
            String postId = node.has("postId") ? node.get("postId").asText() : null;

            NotificationType notificationType = switch (type) {
                case "LIKE" -> NotificationType.LIKE;
                case "COMMENT" -> NotificationType.COMMENT;
                case "FRIEND_REQUEST" -> NotificationType.FRIEND_REQUEST;
                default -> NotificationType.POST_CREATED;
            };

            Notification notification = Notification.builder()
                    .recipientId(recipientId)
                    .senderId(senderId)
                    .type(notificationType)
                    .message(message)
                    .resourceId(postId)
                    .isRead(false)
                    .build();

            notificationService.saveAndSend(notification);

        } catch (Exception e) {
            log.error("Error processing notification event", e);
        }
    }
}
