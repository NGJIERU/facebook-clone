package com.facebook.user.controller;

import com.facebook.user.model.Friendship;
import com.facebook.user.model.UserProfile;
import com.facebook.user.service.FriendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    private String getCurrentUserId() {
        org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                .getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getPrincipal().toString();
    }

    @PostMapping("/request/{addresseeId}")
    public ResponseEntity<Void> sendFriendRequest(
            @PathVariable String addresseeId) {
        String userId = getCurrentUserId();
        friendService.sendFriendRequest(userId, addresseeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accept/{friendshipId}")
    public ResponseEntity<Void> acceptFriendRequest(
            @PathVariable UUID friendshipId) {
        String userId = getCurrentUserId();
        friendService.acceptFriendRequest(friendshipId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reject/{friendshipId}")
    public ResponseEntity<Void> rejectFriendRequest(
            @PathVariable UUID friendshipId) {
        String userId = getCurrentUserId();
        friendService.rejectFriendRequest(friendshipId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getFriends() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<com.facebook.user.dto.FriendRequestDto>> getPendingRequests() {
        String userId = getCurrentUserId();
        return ResponseEntity.ok(friendService.getPendingRequests(userId));
    }

    @DeleteMapping("/{friendId}")
    public ResponseEntity<Void> unfriend(@PathVariable String friendId) {
        String userId = getCurrentUserId();
        friendService.unfriend(userId, friendId);
        return ResponseEntity.ok().build();
    }
}
