package com.swmStrong.demo.domain.group.dto;

import lombok.Builder;

@Builder
public record GroupOwner(
        String userId,
        String nickname
) {
}
