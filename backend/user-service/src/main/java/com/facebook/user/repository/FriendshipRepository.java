package com.facebook.user.repository;

import com.facebook.user.model.Friendship;
import com.facebook.user.model.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    Optional<Friendship> findByRequesterIdAndAddresseeId(String requesterId, String addresseeId);

    @Query("SELECT f FROM Friendship f WHERE (f.requesterId = :userId OR f.addresseeId = :userId) AND f.status = 'ACCEPTED'")
    List<Friendship> findAcceptedFriendships(String userId);

    @Query("SELECT f FROM Friendship f WHERE f.addresseeId = :userId AND f.status = 'PENDING'")
    List<Friendship> findPendingRequests(String userId);

    // Check if any relationship exists between two users
    @Query("SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END FROM Friendship f " +
            "WHERE (f.requesterId = :user1Id AND f.addresseeId = :user2Id) " +
            "OR (f.requesterId = :user2Id AND f.addresseeId = :user1Id)")
    boolean existsBetween(String user1Id, String user2Id);
}
