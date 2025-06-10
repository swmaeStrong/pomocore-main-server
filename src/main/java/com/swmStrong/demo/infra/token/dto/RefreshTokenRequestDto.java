package com.swmStrong.demo.infra.token.dto;

public record RefreshTokenRequestDto(
        String userId,
        String refreshToken
) {
}