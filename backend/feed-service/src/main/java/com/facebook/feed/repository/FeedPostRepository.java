package com.facebook.feed.repository;

import com.facebook.feed.model.FeedPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FeedPostRepository extends JpaRepository<FeedPost, UUID> {
    List<FeedPost> findAllByOrderByCreatedAtDesc();

    List<FeedPost> findByAuthorIdOrderByCreatedAtDesc(String authorId);

    List<FeedPost> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String content);
}
