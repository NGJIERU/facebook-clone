package com.facebook.user.repository;

import com.facebook.user.model.GroupMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GroupMemberRepository extends JpaRepository<GroupMember, UUID> {

    List<GroupMember> findByGroupId(UUID groupId);

    List<GroupMember> findByUserId(String userId);

    Optional<GroupMember> findByGroupIdAndUserId(UUID groupId, String userId);

    boolean existsByGroupIdAndUserId(UUID groupId, String userId);

    @Query("SELECT gm.groupId FROM GroupMember gm WHERE gm.userId = :userId")
    List<UUID> findGroupIdsByUserId(String userId);
}
