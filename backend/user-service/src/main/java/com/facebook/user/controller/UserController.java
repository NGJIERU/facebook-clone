package com.facebook.user.controller;

import com.facebook.user.model.UserProfile;
import com.facebook.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserProfileRepository repository;

    private String getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getPrincipal().toString();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getMyProfile() {
        String userId = getCurrentUserId();
        return repository.findById(UUID.fromString(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserProfile> getUserProfile(@PathVariable UUID id) {
        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserProfile>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(repository.findByUsernameContainingIgnoreCase(query));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<UserProfile> getProfile(@PathVariable String userId) {
        return repository.findById(java.util.UUID.fromString(userId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfile> updateProfile(@RequestBody UserProfile updatedProfile) {
        String userId = getCurrentUserId();
        return repository.findById(java.util.UUID.fromString(userId))
                .map(profile -> {
                    profile.setUsername(updatedProfile.getUsername());
                    profile.setBio(updatedProfile.getBio());
                    profile.setProfilePicUrl(updatedProfile.getProfilePicUrl());
                    profile.setCoverPicUrl(updatedProfile.getCoverPicUrl());
                    return ResponseEntity.ok(repository.save(profile));
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
