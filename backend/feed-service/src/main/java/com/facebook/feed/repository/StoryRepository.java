package com.facebook.feed.repository;

import com.facebook.feed.model.Story;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface StoryRepository extends JpaRepository<Story, UUID> {

    @Query("SELECT s FROM Story s WHERE s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStories(Instant now);

    @Query("SELECT s FROM Story s WHERE s.authorId = :authorId AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStoriesByAuthor(String authorId, Instant now);

    @Query("SELECT s FROM Story s WHERE s.authorId IN :authorIds AND s.expiresAt > :now ORDER BY s.createdAt DESC")
    List<Story> findActiveStoriesByAuthors(List<String> authorIds, Instant now);
}
