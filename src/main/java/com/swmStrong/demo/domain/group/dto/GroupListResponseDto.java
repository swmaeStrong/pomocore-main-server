package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.group.entity.Group;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupListResponseDto(
        Long groupId,
        String name,
        GroupOwner groupOwner,
        List<String> tags,
        String description
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
                .tags(group.getTags())
                .description(group.getDescription())
                .build();
    }
}
