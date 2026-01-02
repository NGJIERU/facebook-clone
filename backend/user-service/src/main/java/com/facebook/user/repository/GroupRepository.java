package com.facebook.user.repository;

import com.facebook.user.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GroupRepository extends JpaRepository<Group, UUID> {

    List<Group> findByIsPublicTrueOrderByMembersCountDesc();

    @Query("SELECT g FROM Group g WHERE LOWER(g.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Group> searchByName(String query);
}
