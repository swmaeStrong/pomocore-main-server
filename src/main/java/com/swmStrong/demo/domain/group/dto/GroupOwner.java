package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.user.entity.User;
import lombok.Builder;

@Builder
public record GroupOwner(
        String userId,
        String nickname
) {
    public static GroupOwner of(User user) {
        return GroupOwner.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }
}
