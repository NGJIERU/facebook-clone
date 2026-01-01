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

import java.time.Instant;
import java.util.List;

@Controller
@RequiredArgsConstructor
@Slf4j
public class FeedController {

    private final FeedPostRepository repository;

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
        String userEmail = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // In real app, we resolve email -> UUID via User Service or JWT claim

        FeedPost post = FeedPost.builder()
                .content(content)
                .imageUrl(imageUrl)
                .authorId(userEmail) // Using email as ID for simple prototype
                .createdAt(Instant.now())
                .likesCount(0)
                .commentsCount(0)
                .build();

        log.info("Creating post for user: {}", userEmail);
        return repository.save(post);
    }
}
