package com.facebook.feed.controller;

import com.facebook.feed.model.FeedPost;
import com.facebook.feed.model.SavedPost;
import com.facebook.feed.repository.FeedPostRepository;
import com.facebook.feed.repository.SavedPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feed/saved")
@RequiredArgsConstructor
@Slf4j
public class SavedPostController {

    private final SavedPostRepository savedPostRepository;
    private final FeedPostRepository feedPostRepository;

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Not authenticated");
        }
        String token = (String) authentication.getCredentials();
        if (token == null) {
            throw new RuntimeException("No token found");
        }
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey))
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("userId", String.class);
    }

    @PostMapping("/{postId}")
    public ResponseEntity<?> savePost(@PathVariable String postId) {
        String userId = getCurrentUserId();
        
        if (savedPostRepository.existsByUserIdAndPostId(userId, postId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Post already saved"));
        }

        SavedPost savedPost = SavedPost.builder()
                .userId(userId)
                .postId(postId)
                .build();
        
        savedPostRepository.save(savedPost);
        return ResponseEntity.ok(Map.of("message", "Post saved successfully"));
    }

    @DeleteMapping("/{postId}")
    public ResponseEntity<?> unsavePost(@PathVariable String postId) {
        String userId = getCurrentUserId();
        savedPostRepository.deleteByUserIdAndPostId(userId, postId);
        return ResponseEntity.ok(Map.of("message", "Post unsaved successfully"));
    }

    @GetMapping
    public ResponseEntity<List<FeedPost>> getSavedPosts() {
        String userId = getCurrentUserId();
        List<SavedPost> savedPosts = savedPostRepository.findByUserIdOrderBySavedAtDesc(userId);
        List<UUID> postIds = savedPosts.stream()
                .map(sp -> UUID.fromString(sp.getPostId()))
                .collect(Collectors.toList());
        
        List<FeedPost> posts = feedPostRepository.findAllById(postIds);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/check/{postId}")
    public ResponseEntity<Map<String, Boolean>> checkSaved(@PathVariable String postId) {
        String userId = getCurrentUserId();
        boolean isSaved = savedPostRepository.existsByUserIdAndPostId(userId, postId);
        return ResponseEntity.ok(Map.of("saved", isSaved));
    }
}
