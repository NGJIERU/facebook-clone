package com.facebook.media.repository;

import com.facebook.media.model.Media;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Media, String> {
    List<Media> findByUserId(String userId);
}
