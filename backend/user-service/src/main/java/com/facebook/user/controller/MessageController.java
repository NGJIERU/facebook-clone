package com.facebook.user.controller;

import com.facebook.user.model.Message;
import com.facebook.user.repository.MessageRepository;
import com.facebook.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final MessageRepository messageRepository;
    private final UserProfileRepository userProfileRepository;

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getPrincipal().toString();
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody Map<String, String> body) {
        String currentUserId = getCurrentUserId();
        String receiverId = body.get("receiverId");
        String content = body.get("content");

        if (receiverId == null || content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("receiverId and content are required");
        }

        Message message = Message.builder()
                .senderId(currentUserId)
                .receiverId(receiverId)
                .content(content.trim())
                .createdAt(Instant.now())
                .read(false)
                .build();

        Message saved = messageRepository.save(message);
        log.info("Message sent from {} to {}", currentUserId, receiverId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/conversation/{partnerId}")
    public ResponseEntity<List<Message>> getConversation(@PathVariable String partnerId) {
        String currentUserId = getCurrentUserId();
        List<Message> messages = messageRepository.findConversation(currentUserId, partnerId);
        
        // Mark messages as read
        messages.stream()
                .filter(m -> m.getReceiverId().equals(currentUserId) && !m.isRead())
                .forEach(m -> {
                    m.setRead(true);
                    messageRepository.save(m);
                });
        
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/conversations")
    public ResponseEntity<?> getConversations() {
        String currentUserId = getCurrentUserId();
        List<Message> latestMessages = messageRepository.findLatestMessagesPerConversation(currentUserId);
        
        // Enrich with user profile info
        var conversations = latestMessages.stream().map(msg -> {
            String partnerId = msg.getSenderId().equals(currentUserId) ? msg.getReceiverId() : msg.getSenderId();
            var profile = userProfileRepository.findById(UUID.fromString(partnerId)).orElse(null);
            return Map.of(
                    "partnerId", partnerId,
                    "partnerName", profile != null ? profile.getUsername() : "Unknown",
                    "partnerPic", profile != null && profile.getProfilePicUrl() != null ? profile.getProfilePicUrl() : "",
                    "lastMessage", msg.getContent(),
                    "lastMessageTime", msg.getCreatedAt().toString(),
                    "isFromMe", msg.getSenderId().equals(currentUserId),
                    "unread", !msg.isRead() && msg.getReceiverId().equals(currentUserId)
            );
        }).toList();
        
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getUnreadCount() {
        String currentUserId = getCurrentUserId();
        int count = messageRepository.findUnreadMessages(currentUserId).size();
        return ResponseEntity.ok(Map.of("count", count));
    }
}
