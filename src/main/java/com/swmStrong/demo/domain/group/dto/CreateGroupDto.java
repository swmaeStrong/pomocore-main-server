package com.swmStrong.demo.domain.group.dto;

import java.util.List;

public record CreateGroupDto(
        String groupName,
        boolean isPublic,
        String groundRule,
        List<String> tags,
        String description
) {
}
