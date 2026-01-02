package com.facebook.user.repository;

import com.facebook.user.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRepository extends JpaRepository<Event, UUID> {

    @Query("SELECT e FROM Event e WHERE e.eventDate > :now ORDER BY e.eventDate ASC")
    List<Event> findUpcomingEvents(Instant now);

    List<Event> findByCreatorIdOrderByEventDateDesc(String creatorId);

    @Query("SELECT e FROM Event e WHERE LOWER(e.title) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<Event> searchByTitle(String query);
}
