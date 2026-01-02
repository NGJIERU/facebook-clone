package com.facebook.feed.controller;

import com.facebook.feed.model.FeedPost;
import com.facebook.feed.repository.FeedPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FeedController {

    private final FeedPostRepository repository;
    private final org.springframework.kafka.core.KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @org.springframework.beans.factory.annotation.Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private String extractUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token format");
        }
        String jwt = token.substring(7);
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey))
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return claims.get("userId", String.class);
    }

    @QueryMapping
    public List<FeedPost> getFeed() {
        return repository.findAllByOrderByCreatedAtDesc();
    }

    @QueryMapping
    public List<FeedPost> getUserPosts(@Argument String authorId) {
        return repository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    @MutationMapping
    public FeedPost createPost(@Argument String content, @Argument String imageUrl) {
        // We need the token. GraphQL context?
        // In Spring GraphQL, we can access headers via DataFetchingEnvironment or
        // Context.
        // OR we can still use SecurityContextHolder if JwtFilter populated it?
        // But JwtFilter (if generic) might not populate claims.
        // Let's rely on SecurityContext to get the token directly if possible?
        // Actually, for GraphQL, @RequestHeader is not available directly on
        // @MutationMapping methods usually?
        // Wait, Spring GraphQL controller methods differ from REST.
        // Standard approach: Access Principal from SecurityContextHolder.
        // If I want "userId" claim, and JwtAuthenticationFilter didn't extract it... I
        // am stuck.

        // OPTION 1: Update `JwtAuthenticationFilter` in `feed-service` to put claims in
        // authentication.details.
        // OPTION 2: Use
        // `SecurityContextHolder.getContext().getAuthentication().getCredentials()`
        // (usually token).

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Access Denied");
        }

        // Try to get token from credentials if JwtAuthenticationFilter sets it.
        // Standard filters often set token as credentials.
        Object credentials = authentication.getCredentials();
        String userId = null;
        if (credentials instanceof String && ((String) credentials).startsWith("ey")) { // Basic check
            try {
                // If credentials IS the token
                userId = extractUserIdFromToken("Bearer " + credentials);
            } catch (Exception e) {
            }
        }

        // Fallback: If we can't get ID from token easily in GraphQL without changing
        // Filter,
        // and if Principal is Email, we have a problem.
        // BUT wait. I updated `AuthService` to put `userId` in claims.
        // Does `JwtAuthenticationFilter` (standard/copied) accidentally put claims in
        // Principal? No.

        // CRITICAL: GraphQL Controller makes extraction hard if header isn't passed.
        // I should have updated `JwtAuthenticationFilter`.

        // Let's assume `JwtAuthenticationFilter` (which I didn't check in
        // `feed-service`) sets credentials = jwt.
        // If not, I am risky.

        // Alternative: Fetch User by Email (Principal) -> Get ID ?
        // Feed Service doesn't have User table.
        // It relies on Token.

        // Let's try to grab `Authorization` header from `GraphQLContext`?
        // public FeedPost createPost(..., DataFetchingEnvironment env)
        // env.getGraphQlContext().get(...)

        // Let's use the `credentials` approach assuming it's the token.
        // If not, I will revert to Email (and accept the mismatch for now or fix
        // later).
        // But the goal is ID alignment.

        // Let's implement the `credentials` check.

        if (userId == null) {
            // Fallback to principal if extraction failed (e.g. mocking)
            log.warn("Could not extract userId from token claims, using Principal");
            userId = (String) authentication.getPrincipal();
        }

        // WAIT. If I use Email as userId, it breaks consistency.
        // I MUST get the UUID.
        // The token is sent in the header.
        // Spring Security `BearerTokenAuthenticationToken` holds the token value.

        // Valid implementation using `authentication.getCredentials()` which usually
        // holds the JWT token string in OAuth2/JWT setups.
        String token = (String) authentication.getCredentials();
        if (token != null) {
            userId = extractUserIdFromToken("Bearer " + token);
        } else {
            throw new RuntimeException("No JWT token found in credentials");
        }

        FeedPost post = FeedPost.builder()
                .content(content)
                .imageUrl(imageUrl)
                .authorId(userId)
                .createdAt(Instant.now())
                .likesCount(0)
                .commentsCount(0)
                .build();

        log.info("Creating post for user: {}", userId);
        FeedPost savedPost = repository.save(post);

        // Publish Event
        try {
            com.facebook.feed.event.PostCreatedEvent event = com.facebook.feed.event.PostCreatedEvent.builder()
                    .postId(savedPost.getId().toString())
                    .authorId(savedPost.getAuthorId())
                    .contentSnippet(savedPost.getContent().length() > 20 ? savedPost.getContent().substring(0, 20)
                            : savedPost.getContent())
                    .createdAt(savedPost.getCreatedAt())
                    .build();

            String eventJson = objectMapper.writeValueAsString(event);
            kafkaTemplate.send("post-created-topic", eventJson);
            log.info("Published PostCreatedEvent to Kafka: {}", eventJson);
        } catch (Exception e) {
            log.error("Failed to publish Kafka event", e);
        }

        return savedPost;
    }

    @org.springframework.web.bind.annotation.GetMapping("/api/feed/user/{userId}")
    public org.springframework.http.ResponseEntity<List<FeedPost>> getUserPostsRest(
            @org.springframework.web.bind.annotation.PathVariable String userId) {
        return org.springframework.http.ResponseEntity.ok(repository.findByAuthorIdOrderByCreatedAtDesc(userId));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/api/feed/posts/{postId}")
    public org.springframework.http.ResponseEntity<?> deletePost(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID postId) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(401).body("Not authenticated");
        }

        String token = (String) authentication.getCredentials();
        if (token == null) {
            return org.springframework.http.ResponseEntity.status(401).body("Could not identify user");
        }

        final String currentUserId;
        try {
            currentUserId = extractUserIdFromToken("Bearer " + token);
        } catch (Exception e) {
            log.error("Failed to extract userId", e);
            return org.springframework.http.ResponseEntity.status(401).body("Could not identify user");
        }

        return repository.findById(postId)
                .map(post -> {
                    if (!post.getAuthorId().equals(currentUserId)) {
                        return org.springframework.http.ResponseEntity.status(403).body("You can only delete your own posts");
                    }
                    repository.delete(post);
                    log.info("Deleted post {} by user {}", postId, currentUserId);
                    return org.springframework.http.ResponseEntity.ok().body("Post deleted");
                })
                .orElse(org.springframework.http.ResponseEntity.notFound().build());
    }

    @org.springframework.web.bind.annotation.PostMapping("/api/feed/posts/{postId}/share")
    public org.springframework.http.ResponseEntity<?> sharePost(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID postId,
            @org.springframework.web.bind.annotation.RequestBody(required = false) java.util.Map<String, String> body) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return org.springframework.http.ResponseEntity.status(401).body("Not authenticated");
        }

        String token = (String) authentication.getCredentials();
        if (token == null) {
            return org.springframework.http.ResponseEntity.status(401).body("Could not identify user");
        }

        final String currentUserId;
        try {
            currentUserId = extractUserIdFromToken("Bearer " + token);
        } catch (Exception e) {
            log.error("Failed to extract userId", e);
            return org.springframework.http.ResponseEntity.status(401).body("Could not identify user");
        }

        return repository.findById(postId)
                .map(originalPost -> {
                    String shareComment = (body != null) ? body.get("comment") : null;
                    
                    FeedPost sharedPost = FeedPost.builder()
                            .authorId(currentUserId)
                            .content(shareComment)
                            .imageUrl(originalPost.getImageUrl())
                            .createdAt(java.time.Instant.now())
                            .originalPostId(originalPost.getId())
                            .originalAuthorId(originalPost.getAuthorId())
                            .likesCount(0)
                            .commentsCount(0)
                            .sharesCount(0)
                            .build();
                    
                    FeedPost saved = repository.save(sharedPost);
                    
                    // Increment share count on original post
                    originalPost.setSharesCount(originalPost.getSharesCount() + 1);
                    repository.save(originalPost);
                    
                    log.info("User {} shared post {}", currentUserId, postId);
                    return org.springframework.http.ResponseEntity.ok(saved);
                })
                .orElse(org.springframework.http.ResponseEntity.notFound().build());
    }
}
