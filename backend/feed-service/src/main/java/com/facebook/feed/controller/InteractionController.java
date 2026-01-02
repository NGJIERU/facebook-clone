package com.facebook.feed.controller;

import com.facebook.feed.model.Comment;
import com.facebook.feed.model.FeedPost;
import com.facebook.feed.model.PostLike;
import com.facebook.feed.repository.CommentRepository;
import com.facebook.feed.repository.FeedPostRepository;
import com.facebook.feed.repository.PostLikeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
@Slf4j
public class InteractionController {

    private final PostLikeRepository likeRepository;
    private final CommentRepository commentRepository;
    private final FeedPostRepository postRepository;
    private final org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    private String getCurrentUserId() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getCredentials() == null) {
            throw new RuntimeException("Not authenticated");
        }
        String token = (String) auth.getCredentials();
        // Extract userId from token
        try {
            String[] parts = token.split("\\.");
            String payload = new String(java.util.Base64.getUrlDecoder().decode(parts[1]));
            var node = objectMapper.readTree(payload);
            return node.get("userId").asText();
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract userId from token");
        }
    }

    // ==================== LIKES ====================

    @PostMapping("/posts/{postId}/like")
    @Transactional
    public ResponseEntity<Map<String, Object>> likePost(@PathVariable UUID postId) {
        String userId = getCurrentUserId();
        log.info("User {} liking post {}", userId, postId);

        if (likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already liked"));
        }

        PostLike like = PostLike.builder()
                .postId(postId)
                .userId(userId)
                .createdAt(Instant.now())
                .build();
        likeRepository.save(like);

        // Update likes count on post
        postRepository.findById(postId).ifPresent(post -> {
            post.setLikesCount(post.getLikesCount() + 1);
            postRepository.save(post);
            
            // Send notification if not own post
            if (!post.getAuthorId().equals(userId)) {
                sendLikeNotification(post, userId);
            }
        });

        int newCount = likeRepository.countByPostId(postId);
        return ResponseEntity.ok(Map.of("liked", true, "likesCount", newCount));
    }

    @DeleteMapping("/posts/{postId}/like")
    @Transactional
    public ResponseEntity<Map<String, Object>> unlikePost(@PathVariable UUID postId) {
        String userId = getCurrentUserId();
        log.info("User {} unliking post {}", userId, postId);

        if (!likeRepository.existsByPostIdAndUserId(postId, userId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Not liked"));
        }

        likeRepository.deleteByPostIdAndUserId(postId, userId);

        // Update likes count on post
        postRepository.findById(postId).ifPresent(post -> {
            post.setLikesCount(Math.max(0, post.getLikesCount() - 1));
            postRepository.save(post);
        });

        int newCount = likeRepository.countByPostId(postId);
        return ResponseEntity.ok(Map.of("liked", false, "likesCount", newCount));
    }

    @GetMapping("/posts/{postId}/like/status")
    public ResponseEntity<Map<String, Object>> getLikeStatus(@PathVariable UUID postId) {
        String userId = getCurrentUserId();
        boolean liked = likeRepository.existsByPostIdAndUserId(postId, userId);
        int count = likeRepository.countByPostId(postId);
        return ResponseEntity.ok(Map.of("liked", liked, "likesCount", count));
    }

    // ==================== COMMENTS ====================

    @PostMapping("/posts/{postId}/comments")
    @Transactional
    public ResponseEntity<Comment> addComment(@PathVariable UUID postId, @RequestBody Map<String, String> body) {
        String userId = getCurrentUserId();
        String content = body.get("content");
        
        if (content == null || content.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("User {} commenting on post {}", userId, postId);

        Comment comment = Comment.builder()
                .postId(postId)
                .authorId(userId)
                .content(content.trim())
                .createdAt(Instant.now())
                .build();
        Comment saved = commentRepository.save(comment);

        // Update comments count on post
        postRepository.findById(postId).ifPresent(post -> {
            post.setCommentsCount(post.getCommentsCount() + 1);
            postRepository.save(post);
            
            // Send notification if not own post
            if (!post.getAuthorId().equals(userId)) {
                sendCommentNotification(post, userId, content);
            }
        });

        return ResponseEntity.ok(saved);
    }

    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable UUID postId) {
        List<Comment> comments = commentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return ResponseEntity.ok(comments);
    }

    // ==================== NOTIFICATIONS ====================

    private void sendLikeNotification(FeedPost post, String likerId) {
        try {
            Map<String, Object> notification = Map.of(
                "type", "LIKE",
                "recipientId", post.getAuthorId(),
                "senderId", likerId,
                "postId", post.getId().toString(),
                "message", "liked your post",
                "timestamp", Instant.now().toString()
            );
            String json = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send("notification-topic", json);
            log.info("Sent like notification: {}", json);
        } catch (Exception e) {
            log.error("Failed to send like notification", e);
        }
    }

    private void sendCommentNotification(FeedPost post, String commenterId, String commentContent) {
        try {
            String snippet = commentContent.length() > 50 ? commentContent.substring(0, 50) + "..." : commentContent;
            Map<String, Object> notification = Map.of(
                "type", "COMMENT",
                "recipientId", post.getAuthorId(),
                "senderId", commenterId,
                "postId", post.getId().toString(),
                "message", "commented: " + snippet,
                "timestamp", Instant.now().toString()
            );
            String json = objectMapper.writeValueAsString(notification);
            kafkaTemplate.send("notification-topic", json);
            log.info("Sent comment notification: {}", json);
        } catch (Exception e) {
            log.error("Failed to send comment notification", e);
        }
    }
}
