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

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getMyProfile(@RequestHeader("Authorization") String token) {
        String userId = extractUserIdFromToken(token);
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
}
