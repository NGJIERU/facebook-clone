package com.facebook.user.controller;

import com.facebook.user.model.Event;
import com.facebook.user.model.EventRsvp;
import com.facebook.user.repository.EventRepository;
import com.facebook.user.repository.EventRsvpRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Slf4j
public class EventController {

    private final EventRepository eventRepository;
    private final EventRsvpRepository rsvpRepository;

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getPrincipal().toString();
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody Map<String, Object> body) {
        String currentUserId = getCurrentUserId();
        String title = (String) body.get("title");
        String description = (String) body.get("description");
        String location = (String) body.get("location");
        String coverImageUrl = (String) body.get("coverImageUrl");
        String eventDateStr = (String) body.get("eventDate");

        if (title == null || title.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Event title is required");
        }
        if (eventDateStr == null) {
            return ResponseEntity.badRequest().body("Event date is required");
        }

        Instant eventDate = Instant.parse(eventDateStr);

        Event event = Event.builder()
                .title(title.trim())
                .description(description)
                .location(location)
                .coverImageUrl(coverImageUrl)
                .creatorId(currentUserId)
                .eventDate(eventDate)
                .createdAt(Instant.now())
                .attendeesCount(0)
                .interestedCount(0)
                .build();

        Event saved = eventRepository.save(event);
        log.info("Event '{}' created by user {}", title, currentUserId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Event>> getUpcomingEvents() {
        return ResponseEntity.ok(eventRepository.findUpcomingEvents(Instant.now()));
    }

    @GetMapping("/my")
    public ResponseEntity<List<Event>> getMyEvents() {
        String currentUserId = getCurrentUserId();
        List<UUID> eventIds = rsvpRepository.findEventIdsByUserId(currentUserId);
        List<Event> events = eventRepository.findAllById(eventIds);
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<?> getEvent(@PathVariable UUID eventId) {
        return eventRepository.findById(eventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Event>> searchEvents(@RequestParam String query) {
        return ResponseEntity.ok(eventRepository.searchByTitle(query));
    }

    @PostMapping("/{eventId}/rsvp")
    public ResponseEntity<?> rsvpEvent(@PathVariable UUID eventId, @RequestBody Map<String, String> body) {
        String currentUserId = getCurrentUserId();
        String status = body.get("status"); // GOING or INTERESTED

        if (status == null || (!status.equals("GOING") && !status.equals("INTERESTED"))) {
            return ResponseEntity.badRequest().body("Status must be GOING or INTERESTED");
        }

        return eventRepository.findById(eventId)
                .map(event -> {
                    var existingRsvp = rsvpRepository.findByEventIdAndUserId(eventId, currentUserId);
                    
                    if (existingRsvp.isPresent()) {
                        EventRsvp rsvp = existingRsvp.get();
                        String oldStatus = rsvp.getStatus();
                        rsvp.setStatus(status);
                        rsvpRepository.save(rsvp);
                        
                        // Update counts
                        if (!oldStatus.equals(status)) {
                            if (oldStatus.equals("GOING")) event.setAttendeesCount(event.getAttendeesCount() - 1);
                            if (oldStatus.equals("INTERESTED")) event.setInterestedCount(event.getInterestedCount() - 1);
                            if (status.equals("GOING")) event.setAttendeesCount(event.getAttendeesCount() + 1);
                            if (status.equals("INTERESTED")) event.setInterestedCount(event.getInterestedCount() + 1);
                            eventRepository.save(event);
                        }
                    } else {
                        EventRsvp rsvp = EventRsvp.builder()
                                .eventId(eventId)
                                .userId(currentUserId)
                                .status(status)
                                .createdAt(Instant.now())
                                .build();
                        rsvpRepository.save(rsvp);
                        
                        if (status.equals("GOING")) event.setAttendeesCount(event.getAttendeesCount() + 1);
                        if (status.equals("INTERESTED")) event.setInterestedCount(event.getInterestedCount() + 1);
                        eventRepository.save(event);
                    }

                    log.info("User {} RSVP'd {} to event {}", currentUserId, status, eventId);
                    return ResponseEntity.ok(Map.of("message", "RSVP updated", "status", status));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{eventId}/rsvp")
    public ResponseEntity<?> cancelRsvp(@PathVariable UUID eventId) {
        String currentUserId = getCurrentUserId();

        return rsvpRepository.findByEventIdAndUserId(eventId, currentUserId)
                .map(rsvp -> {
                    String status = rsvp.getStatus();
                    rsvpRepository.delete(rsvp);

                    eventRepository.findById(eventId).ifPresent(event -> {
                        if (status.equals("GOING")) event.setAttendeesCount(Math.max(0, event.getAttendeesCount() - 1));
                        if (status.equals("INTERESTED")) event.setInterestedCount(Math.max(0, event.getInterestedCount() - 1));
                        eventRepository.save(event);
                    });

                    log.info("User {} cancelled RSVP for event {}", currentUserId, eventId);
                    return ResponseEntity.ok(Map.of("message", "RSVP cancelled"));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{eventId}/rsvp/status")
    public ResponseEntity<?> getRsvpStatus(@PathVariable UUID eventId) {
        String currentUserId = getCurrentUserId();
        var rsvp = rsvpRepository.findByEventIdAndUserId(eventId, currentUserId);
        java.util.HashMap<String, Object> response = new java.util.HashMap<>();
        response.put("hasRsvp", rsvp.isPresent());
        response.put("status", rsvp.map(EventRsvp::getStatus).orElse(null));
        return ResponseEntity.ok(response);
    }
}
