package com.facebook.user.service;

import com.facebook.user.dto.FriendRequestDto;
import com.facebook.user.model.Friendship;
import com.facebook.user.model.FriendshipStatus;
import com.facebook.user.model.UserProfile;
import com.facebook.user.repository.FriendshipRepository;
import com.facebook.user.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserProfileRepository userProfileRepository;

    @Transactional
    public void sendFriendRequest(String requesterId, String addresseeId) {
        if (requesterId.equals(addresseeId)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        if (friendshipRepository.existsBetween(requesterId, addresseeId)) {
            throw new IllegalArgumentException("Friendship or request already exists");
        }

        Friendship friendship = Friendship.builder()
                .requesterId(requesterId)
                .addresseeId(addresseeId)
                .status(FriendshipStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        friendshipRepository.save(friendship);
        log.info("Friend request sent from {} to {}", requesterId, addresseeId);
    }

    @Transactional
    public void acceptFriendRequest(UUID friendshipId, String userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));

        if (!friendship.getAddresseeId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to accept this request");
        }

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalArgumentException("Friendship is not in pending state");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendshipRepository.save(friendship);
        log.info("Friendship accepted: {}", friendshipId);
    }

    @Transactional
    public void rejectFriendRequest(UUID friendshipId, String userId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));

        if (!friendship.getAddresseeId().equals(userId) && !friendship.getRequesterId().equals(userId)) {
            throw new IllegalArgumentException("You are not authorized to reject/cancel this request");
        }

        friendship.setStatus(FriendshipStatus.REJECTED); // Or delete it: friendshipRepository.delete(friendship);
        friendshipRepository.save(friendship);
        log.info("Friendship rejected/cancelled: {}", friendshipId);
    }

    public List<UserProfile> getFriends(String userId) {
        List<Friendship> friendships = friendshipRepository.findAcceptedFriendships(userId);
        List<UUID> friendIds = new ArrayList<>();

        for (Friendship f : friendships) {
            if (f.getRequesterId().equals(userId)) {
                try {
                    friendIds.add(UUID.fromString(f.getAddresseeId()));
                } catch (IllegalArgumentException e) {
                    log.error("Invalid UUID for addressee: {}", f.getAddresseeId());
                }
            } else {
                try {
                    friendIds.add(UUID.fromString(f.getRequesterId()));
                } catch (IllegalArgumentException e) {
                    log.error("Invalid UUID for requester: {}", f.getRequesterId());
                }
            }
        }

        return userProfileRepository.findAllById(friendIds);
    }

    public List<FriendRequestDto> getPendingRequests(String userId) {
        List<Friendship> requests = friendshipRepository.findPendingRequests(userId);

        if (requests.isEmpty()) {
            return new ArrayList<>();
        }

        List<UUID> requesterIds = requests.stream()
                .map(f -> {
                    try {
                        return UUID.fromString(f.getRequesterId());
                    } catch (IllegalArgumentException e) {
                        log.error("Invalid UUID for requester in pending request: {}", f.getRequesterId());
                        return null;
                    }
                })
                .filter(java.util.Objects::nonNull)
                .toList();

        Map<UUID, UserProfile> profiles = userProfileRepository.findAllById(requesterIds)
                .stream()
                .collect(java.util.stream.Collectors.toMap(UserProfile::getId, p -> p));

        return requests.stream().map(r -> {
            UserProfile profile = profiles.get(UUID.fromString(r.getRequesterId()));
            String name = (profile != null) ? profile.getUsername() : "Unknown User";

            return FriendRequestDto.builder()
                    .id(r.getId())
                    .requesterId(r.getRequesterId())
                    .requesterName(name)
                    .createdAt(r.getCreatedAt())
                    .build();
        }).toList();
    }

    @Transactional
    public void unfriend(String userId, String friendId) {
        Friendship friendship = friendshipRepository.findAcceptedBetween(userId, friendId)
                .orElseThrow(() -> new IllegalArgumentException("Friendship not found"));
        
        friendshipRepository.delete(friendship);
        log.info("Unfriended: {} and {}", userId, friendId);
    }
}
