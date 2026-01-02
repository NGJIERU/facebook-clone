package com.facebook.feed.controller;

import com.facebook.feed.model.FeedPost;
import com.facebook.feed.model.SavedPost;
import com.facebook.feed.repository.FeedPostRepository;
import com.facebook.feed.repository.SavedPostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/feed/saved")
@RequiredArgsConstructor
public class SavedPostController {

    private final SavedPostRepository savedPostRepository;
    private final FeedPostRepository feedPostRepository;

    @PostMapping("/{postId}")
    public ResponseEntity<?> savePost(
            @PathVariable String postId,
            @RequestHeader("X-User-Id") String userId) {
        
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
    public ResponseEntity<?> unsavePost(
            @PathVariable String postId,
            @RequestHeader("X-User-Id") String userId) {
        
        savedPostRepository.deleteByUserIdAndPostId(userId, postId);
        return ResponseEntity.ok(Map.of("message", "Post unsaved successfully"));
    }

    @GetMapping
    public ResponseEntity<List<FeedPost>> getSavedPosts(@RequestHeader("X-User-Id") String userId) {
        List<SavedPost> savedPosts = savedPostRepository.findByUserIdOrderBySavedAtDesc(userId);
        List<UUID> postIds = savedPosts.stream()
                .map(sp -> UUID.fromString(sp.getPostId()))
                .collect(Collectors.toList());
        
        List<FeedPost> posts = feedPostRepository.findAllById(postIds);
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/check/{postId}")
    public ResponseEntity<Map<String, Boolean>> checkSaved(
            @PathVariable String postId,
            @RequestHeader("X-User-Id") String userId) {
        
        boolean isSaved = savedPostRepository.existsByUserIdAndPostId(userId, postId);
        return ResponseEntity.ok(Map.of("saved", isSaved));
    }
}
