package com.swmStrong.demo.domain.group.dto;

public record CreateGroupDto(
        String groupName,
        boolean isPublic,
        String groundRule,
        String tag,
        String description
) {
}
