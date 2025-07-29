package com.swmStrong.demo.domain.group.dto;

import java.util.List;

public record UpdateGroupDto(
        String description,
        List<String> tags,
        String groundRule,
        String name,
        Boolean isPublic
) {
}
