package com.swmStrong.demo.util.token.dto;

public record RefreshTokenRequestDto(
        String userId,
        String refreshToken
) {
}