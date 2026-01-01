package com.facebook.user.controller;

import com.facebook.user.model.UserProfile;
import com.facebook.user.repository.UserProfileRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
@Slf4j
public class UserGraphQLController {

    private final UserProfileRepository repository;

    @QueryMapping
    public UserProfile me() {
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        // In a real app, we might store the UUID in the JWT claim 'sub' vs email, or
        // look it up.
        // For now, assuming email is username or using Repo to find by username.
        log.info("Fetching profile for: {}", email);
        return repository.findByUsername(email)
                .orElseThrow(() -> new RuntimeException("Profile not found for " + email));
    }

    @QueryMapping
    public UserProfile getUser(@Argument String id) {
        return repository.findById(UUID.fromString(id))
                .orElse(null);
    }

    @MutationMapping
    public UserProfile createProfile(@Argument ProfileInput input) {
        // Authenticated user
        String email = (String) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Check if profile exists
        if (repository.findByUsername(email).isPresent()) {
            throw new RuntimeException("Profile already exists");
        }

        UserProfile profile = UserProfile.builder()
                .id(UUID.fromString(input.getId())) // Auth Service ID
                .username(email) // Default username to email initially
                .bio(input.getBio())
                .profilePicUrl(input.getProfilePicUrl())
                .build();
        return repository.save(profile);
    }

    @Data
    static class ProfileInput {
        private String id;
        private String username;
        private String bio;
        private String profilePicUrl;
        private String coverPicUrl;
    }
}
