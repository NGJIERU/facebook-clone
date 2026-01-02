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

    @org.springframework.beans.factory.annotation.Value("${application.security.jwt.secret-key}")
    private String secretKey;

    private String extractUserIdFromToken(String token) {
        if (token == null || !token.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid token format");
        }
        String jwt = token.substring(7);
        io.jsonwebtoken.Claims claims = io.jsonwebtoken.Jwts.parserBuilder()
                .setSigningKey(io.jsonwebtoken.io.Decoders.BASE64.decode(secretKey))
                .build()
                .parseClaimsJws(jwt)
                .getBody();
        return claims.get("userId", String.class);
    }

    @PostMapping("/request/{addresseeId}")
    public ResponseEntity<Void> sendFriendRequest(
            @PathVariable String addresseeId,
            @RequestHeader("Authorization") String token) {
        String userId = extractUserIdFromToken(token);
        friendService.sendFriendRequest(userId, addresseeId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/accept/{friendshipId}")
    public ResponseEntity<Void> acceptFriendRequest(
            @PathVariable UUID friendshipId,
            @RequestHeader("Authorization") String token) {
        String userId = extractUserIdFromToken(token);
        friendService.acceptFriendRequest(friendshipId, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reject/{friendshipId}")
    public ResponseEntity<Void> rejectFriendRequest(
            @PathVariable UUID friendshipId,
            @RequestHeader("Authorization") String token) {
        String userId = extractUserIdFromToken(token);
        friendService.rejectFriendRequest(friendshipId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<List<UserProfile>> getFriends(@RequestHeader("Authorization") String token) {
        String userId = extractUserIdFromToken(token);
        return ResponseEntity.ok(friendService.getFriends(userId));
    }

    @GetMapping("/requests")
    public ResponseEntity<List<com.facebook.user.dto.FriendRequestDto>> getPendingRequests(
            @RequestHeader("Authorization") String token) {
        String userId = extractUserIdFromToken(token);
        return ResponseEntity.ok(friendService.getPendingRequests(userId));
    }
}
