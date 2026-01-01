package com.facebook.notification.service;

import com.facebook.notification.model.Notification;
import com.facebook.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository repository;
    private final SimpMessagingTemplate messagingTemplate;

    public void saveAndSend(Notification notification) {
        Notification saved = repository.save(notification);
        log.info("Saved notification: {}", saved);

        // Send to specific user via WebSocket
        // Destination: /topic/notifications/{userId}
        messagingTemplate.convertAndSend("/topic/notifications/" + saved.getRecipientId(), saved);
        log.info("Sent notification to WebSocket: /topic/notifications/{}", saved.getRecipientId());
    }

    public List<Notification> getNotificationsForUser(String userId) {
        return repository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long id) {
        repository.findById(id).ifPresent(notification -> {
            notification.setRead(true);
            repository.save(notification);
        });
    }
}
