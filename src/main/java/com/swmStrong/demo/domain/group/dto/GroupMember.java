package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.user.entity.User;
import lombok.Builder;

@Builder
public record GroupMember(
        String userId,
        String nickname
) {
    public static GroupMember of(User user) {
        return GroupMember.builder()
                .userId(user.getId())
                .nickname(user.getNickname())
                .build();
    }
}
