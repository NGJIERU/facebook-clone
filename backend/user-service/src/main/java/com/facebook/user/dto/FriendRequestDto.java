package com.facebook.user.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class FriendRequestDto {
    private UUID id;
    private String requesterId;
    private String requesterName;
    private Instant createdAt;
}
