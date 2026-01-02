package com.facebook.feed.controller;

import com.facebook.feed.model.Story;
import com.facebook.feed.repository.StoryRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.jsonwebtoken.io.Decoders;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/feed/stories")
@RequiredArgsConstructor
@Slf4j
public class StoryController {

    private final StoryRepository storyRepository;

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private String extractUserIdFromToken(String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(keyBytes))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", String.class);
    }

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }
        String token = (String) authentication.getCredentials();
        if (token == null) {
            throw new RuntimeException("No token found");
        }
        return extractUserIdFromToken("Bearer " + token);
    }

    @PostMapping
    public ResponseEntity<?> createStory(@RequestBody Map<String, String> body) {
        String currentUserId = getCurrentUserId();
        String imageUrl = body.get("imageUrl");
        String text = body.get("text");

        if ((imageUrl == null || imageUrl.isEmpty()) && (text == null || text.isEmpty())) {
            return ResponseEntity.badRequest().body("Either imageUrl or text is required");
        }

        Instant now = Instant.now();
        Story story = Story.builder()
                .authorId(currentUserId)
                .imageUrl(imageUrl)
                .text(text)
                .createdAt(now)
                .expiresAt(now.plus(24, ChronoUnit.HOURS))
                .viewsCount(0)
                .build();

        Story saved = storyRepository.save(story);
        log.info("Story created by user {}", currentUserId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Story>> getActiveStories() {
        List<Story> stories = storyRepository.findActiveStories(Instant.now());
        return ResponseEntity.ok(stories);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Story>> getUserStories(@PathVariable String userId) {
        List<Story> stories = storyRepository.findActiveStoriesByAuthor(userId, Instant.now());
        return ResponseEntity.ok(stories);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Story>> getMyStories() {
        String currentUserId = getCurrentUserId();
        List<Story> stories = storyRepository.findActiveStoriesByAuthor(currentUserId, Instant.now());
        return ResponseEntity.ok(stories);
    }

    @PostMapping("/{storyId}/view")
    public ResponseEntity<?> viewStory(@PathVariable UUID storyId) {
        return storyRepository.findById(storyId)
                .map(story -> {
                    story.setViewsCount(story.getViewsCount() + 1);
                    storyRepository.save(story);
                    return ResponseEntity.ok(Map.of("viewsCount", story.getViewsCount()));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{storyId}")
    public ResponseEntity<?> deleteStory(@PathVariable UUID storyId) {
        String currentUserId = getCurrentUserId();
        return storyRepository.findById(storyId)
                .map(story -> {
                    if (!story.getAuthorId().equals(currentUserId)) {
                        return ResponseEntity.status(403).body("You can only delete your own stories");
                    }
                    storyRepository.delete(story);
                    return ResponseEntity.ok().body("Story deleted");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
