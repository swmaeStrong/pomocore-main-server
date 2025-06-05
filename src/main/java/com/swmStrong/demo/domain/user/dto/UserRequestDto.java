package com.swmStrong.demo.domain.user.dto;

public record UserRequestDto(
        String userId,
        String nickname
) {
    public static UserRequestDto of(String userId, String nickname) {
        return new UserRequestDto(userId, nickname);
    }
}
