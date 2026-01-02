package com.facebook.user.repository;

import com.facebook.user.model.EventRsvp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EventRsvpRepository extends JpaRepository<EventRsvp, UUID> {

    List<EventRsvp> findByEventId(UUID eventId);

    List<EventRsvp> findByUserId(String userId);

    Optional<EventRsvp> findByEventIdAndUserId(UUID eventId, String userId);

    @Query("SELECT r.eventId FROM EventRsvp r WHERE r.userId = :userId")
    List<UUID> findEventIdsByUserId(String userId);

    int countByEventIdAndStatus(UUID eventId, String status);
}
