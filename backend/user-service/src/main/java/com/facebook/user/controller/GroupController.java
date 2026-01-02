package com.facebook.user.controller;

import com.facebook.user.model.Group;
import com.facebook.user.model.GroupMember;
import com.facebook.user.repository.GroupMemberRepository;
import com.facebook.user.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Slf4j
public class GroupController {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository memberRepository;

    private String getCurrentUserId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User not authenticated");
        }
        return authentication.getPrincipal().toString();
    }

    @PostMapping
    public ResponseEntity<?> createGroup(@RequestBody Map<String, Object> body) {
        String currentUserId = getCurrentUserId();
        String name = (String) body.get("name");
        String description = (String) body.get("description");
        String coverImageUrl = (String) body.get("coverImageUrl");
        Boolean isPublic = body.get("isPublic") != null ? (Boolean) body.get("isPublic") : true;

        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Group name is required");
        }

        Group group = Group.builder()
                .name(name.trim())
                .description(description)
                .coverImageUrl(coverImageUrl)
                .creatorId(currentUserId)
                .createdAt(Instant.now())
                .membersCount(1)
                .isPublic(isPublic)
                .build();

        Group saved = groupRepository.save(group);

        // Add creator as admin member
        GroupMember member = GroupMember.builder()
                .groupId(saved.getId())
                .userId(currentUserId)
                .role("ADMIN")
                .joinedAt(Instant.now())
                .build();
        memberRepository.save(member);

        log.info("Group '{}' created by user {}", name, currentUserId);
        return ResponseEntity.ok(saved);
    }

    @GetMapping
    public ResponseEntity<List<Group>> getPublicGroups() {
        return ResponseEntity.ok(groupRepository.findByIsPublicTrueOrderByMembersCountDesc());
    }

    @GetMapping("/my")
    public ResponseEntity<List<Group>> getMyGroups() {
        String currentUserId = getCurrentUserId();
        List<UUID> groupIds = memberRepository.findGroupIdsByUserId(currentUserId);
        List<Group> groups = groupRepository.findAllById(groupIds);
        return ResponseEntity.ok(groups);
    }

    @GetMapping("/{groupId}")
    public ResponseEntity<?> getGroup(@PathVariable UUID groupId) {
        return groupRepository.findById(groupId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<List<Group>> searchGroups(@RequestParam String query) {
        return ResponseEntity.ok(groupRepository.searchByName(query));
    }

    @PostMapping("/{groupId}/join")
    public ResponseEntity<?> joinGroup(@PathVariable UUID groupId) {
        String currentUserId = getCurrentUserId();

        if (memberRepository.existsByGroupIdAndUserId(groupId, currentUserId)) {
            return ResponseEntity.badRequest().body("Already a member");
        }

        return groupRepository.findById(groupId)
                .map(group -> {
                    GroupMember member = GroupMember.builder()
                            .groupId(groupId)
                            .userId(currentUserId)
                            .role("MEMBER")
                            .joinedAt(Instant.now())
                            .build();
                    memberRepository.save(member);

                    group.setMembersCount(group.getMembersCount() + 1);
                    groupRepository.save(group);

                    log.info("User {} joined group {}", currentUserId, groupId);
                    return ResponseEntity.ok(Map.of("message", "Joined group successfully"));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{groupId}/leave")
    public ResponseEntity<?> leaveGroup(@PathVariable UUID groupId) {
        String currentUserId = getCurrentUserId();

        return memberRepository.findByGroupIdAndUserId(groupId, currentUserId)
                .map(member -> {
                    memberRepository.delete(member);

                    groupRepository.findById(groupId).ifPresent(group -> {
                        group.setMembersCount(Math.max(0, group.getMembersCount() - 1));
                        groupRepository.save(group);
                    });

                    log.info("User {} left group {}", currentUserId, groupId);
                    return ResponseEntity.ok(Map.of("message", "Left group successfully"));
                })
                .orElse(ResponseEntity.badRequest().build());
    }

    @GetMapping("/{groupId}/members")
    public ResponseEntity<List<GroupMember>> getGroupMembers(@PathVariable UUID groupId) {
        return ResponseEntity.ok(memberRepository.findByGroupId(groupId));
    }

    @GetMapping("/{groupId}/membership")
    public ResponseEntity<?> checkMembership(@PathVariable UUID groupId) {
        String currentUserId = getCurrentUserId();
        boolean isMember = memberRepository.existsByGroupIdAndUserId(groupId, currentUserId);
        return ResponseEntity.ok(Map.of("isMember", isMember));
    }
}
