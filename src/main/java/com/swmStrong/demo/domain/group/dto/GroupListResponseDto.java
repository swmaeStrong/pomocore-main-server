package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.group.entity.Group;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record GroupListResponseDto(
        Long groupId,
        String name,
        GroupOwner groupOwner,
        List<String> tags,
        String description,
        int memberCount,
        boolean isPublic,
        LocalDateTime createdAt
) {
    public static GroupListResponseDto of(Group group) {
        return GroupListResponseDto.builder()
                .groupId(group.getId())
                .name(group.getName())
                .groupOwner(
                        GroupOwner.builder()
                                .userId(group.getOwner().getId())
                                .nickname(group.getOwner().getNickname())
                                .build()
                )
                .isPublic(group.isPublic())
                .memberCount(group.getMemberCount())
                .tags(group.getTags())
                .description(group.getDescription())
                .createdAt(group.getCreatedAt())
                .build();
    }
}
