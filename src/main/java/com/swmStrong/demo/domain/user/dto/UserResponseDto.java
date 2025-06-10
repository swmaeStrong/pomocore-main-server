package com.swmStrong.demo.domain.user.dto;

import com.swmStrong.demo.domain.user.entity.User;

public record UserResponseDto(
        String userId,
        String nickname
) {
    public static UserResponseDto of(User user) {
        return new UserResponseDto(user.getId(), user.getNickname());
    }
}
