package com.swmStrong.demo.domain.user.dto;

public record UserRequestDto(
        String userId
) {
    public static UserRequestDto of(String userId) {
        return new UserRequestDto(userId);
    }
}
