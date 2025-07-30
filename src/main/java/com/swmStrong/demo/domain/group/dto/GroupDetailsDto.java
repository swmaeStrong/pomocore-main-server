package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.group.entity.Group;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record GroupDetailsDto(
        GroupOwner owner,
        String name,
        List<String> tags,
        String description,
        String groundRule,
        boolean isPublic,
        LocalDate createdAt
) {

    public static GroupDetailsDto of(Group group) {
        return GroupDetailsDto.builder()
                .owner(GroupOwner.of(group.getOwner()))
                .name(group.getName())
                .tags(group.getTags())
                .description(group.getDescription())
                .groundRule(group.getGroundRule())
                .isPublic(group.isPublic())
                .createdAt(group.getCreatedAt().toLocalDate())
                .build();
    }
}
