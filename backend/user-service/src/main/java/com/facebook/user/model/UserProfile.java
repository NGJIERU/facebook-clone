package com.facebook.user.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    private UUID id; // Shared ID with Auth Service

    @Column(nullable = false, unique = true)
    private String username;

    private String bio;
    private String profilePicUrl;
    private String coverPicUrl;
}
