package com.facebook.feed.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "feed_posts")
public class FeedPost {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String authorId; // Reference to User Service (no FK constraint across microservices)

    @Column(columnDefinition = "TEXT")
    private String content;

    private String imageUrl;

    @Column(nullable = false)
    private Instant createdAt;

    private int likesCount;
    private int commentsCount;
}
