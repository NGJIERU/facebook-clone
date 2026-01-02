package com.facebook.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String location;

    private String coverImageUrl;

    @Column(nullable = false)
    private String creatorId;

    @Column(nullable = false)
    private Instant eventDate;

    @Column(nullable = false)
    private Instant createdAt;

    private int attendeesCount;

    private int interestedCount;
}
