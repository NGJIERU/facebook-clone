package com.facebook.feed.repository;

import com.facebook.feed.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {
    Optional<PostLike> findByPostIdAndUserId(UUID postId, String userId);
    boolean existsByPostIdAndUserId(UUID postId, String userId);
    int countByPostId(UUID postId);
    void deleteByPostIdAndUserId(UUID postId, String userId);
}
