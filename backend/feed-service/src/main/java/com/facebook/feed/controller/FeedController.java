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
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null ||
                !authentication.isAuthenticated() ||
                "anonymousUser".equals(authentication.getPrincipal())) {
            throw new RuntimeException("Access Denied: You must be logged in to create a post.");
        }

        String userEmail = (String) authentication.getPrincipal();

        FeedPost post = FeedPost.builder()
                .content(content)
                .imageUrl(imageUrl)
                .authorId(userEmail)
                .createdAt(Instant.now())
                .likesCount(0)
                .commentsCount(0)
                .build();

        log.info("Creating post for user: {}", userEmail);
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
}
