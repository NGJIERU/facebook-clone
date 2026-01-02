package com.facebook.media.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "media")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String contentType;

    @Column(nullable = false)
    private Long size;

    @Column(nullable = false)
    private String url;

    @CreationTimestamp
    private LocalDateTime createdAt;
}
