package com.facebook.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatConsumerService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "chat-messages", groupId = "notification-chat-group")
    public void consumeChatMessage(String eventJson) {
        try {
            log.info("Received Chat Event: {}", eventJson);
            // We just forward the JSON directly to the frontend via WebSocket
            // Parse it to verify structure if needed, or extract recipient
            var node = objectMapper.readTree(eventJson);
            String recipientId = node.get("receiverId").asText();

            // Push to /topic/chat/{recipientId}
            messagingTemplate.convertAndSend("/topic/chat/" + recipientId, eventJson);
            log.info("Pushed Message to WebSocket: /topic/chat/{}", recipientId);

            // OPTIONAL: Also push to sender so they see confirmation?
            // Usually frontend handles optimistic UI, but for syncing multi-device:
            // String senderId = node.get("senderId").asText();
            // messagingTemplate.convertAndSend("/topic/chat/" + senderId, eventJson);

        } catch (Exception e) {
            log.error("Error processing Chat Kafka event", e);
        }
    }
}
