package com.facebook.user.repository;

import com.facebook.user.model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {

        @Query("SELECT m FROM Message m WHERE (m.senderId = :userId1 AND m.receiverId = :userId2) " +
                        "OR (m.senderId = :userId2 AND m.receiverId = :userId1) ORDER BY m.createdAt ASC")
        List<Message> findConversation(String userId1, String userId2);

        @Query("SELECT m FROM Message m WHERE m.receiverId = :userId AND m.read = false")
        List<Message> findUnreadMessages(String userId);

        @Query("SELECT DISTINCT CASE WHEN m.senderId = :userId THEN m.receiverId ELSE m.senderId END " +
                        "FROM Message m WHERE m.senderId = :userId OR m.receiverId = :userId")
        List<String> findConversationPartners(String userId);

        @Query("SELECT m FROM Message m WHERE m.createdAt IN " +
                        "(SELECT MAX(m2.createdAt) FROM Message m2 WHERE m2.senderId = :userId OR m2.receiverId = :userId "
                        +
                        "GROUP BY CASE WHEN m2.senderId = :userId THEN m2.receiverId ELSE m2.senderId END) " +
                        "ORDER BY m.createdAt DESC")
        List<Message> findLatestMessagesPerConversation(String userId);
}
