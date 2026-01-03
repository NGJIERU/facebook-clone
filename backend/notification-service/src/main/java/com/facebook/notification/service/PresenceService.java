package com.facebook.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class PresenceService {

    private final RedisTemplate<String, String> redisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private static final String ONLINE_USERS_KEY = "online_users";

    public void connectUser(String userId) {
        if (userId == null)
            return;
        redisTemplate.opsForSet().add(ONLINE_USERS_KEY, userId);
        log.info("User connected: {}", userId);
        broadcastPresence(userId, true);
    }

    public void disconnectUser(String userId) {
        if (userId == null)
            return;
        redisTemplate.opsForSet().remove(ONLINE_USERS_KEY, userId);
        log.info("User disconnected: {}", userId);
        broadcastPresence(userId, false);
    }

    private void broadcastPresence(String userId, boolean isOnline) {
        // Broadcast to a public presence topic for MVP (in prod, direct to friends
        // only)
        messagingTemplate.convertAndSend("/topic/presence", new PresenceUpdate(userId, isOnline));
    }

    @lombok.Data
    @lombok.AllArgsConstructor
    public static class PresenceUpdate {
        private String userId;
        private boolean online;
    }

    public boolean isUserOnline(String userId) {
        if (userId == null)
            return false;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(ONLINE_USERS_KEY, userId));
    }

    public Set<String> getOnlineUsers() {
        return redisTemplate.opsForSet().members(ONLINE_USERS_KEY);
    }
}
