package com.swmStrong.demo.util.token.dto;

public record TokenResponseDto(
        String accessToken,
        String refreshToken
) {
    public static TokenResponseDto of(String accessToken, String refreshToken) {
        return new TokenResponseDto(accessToken, refreshToken);
    }
}
