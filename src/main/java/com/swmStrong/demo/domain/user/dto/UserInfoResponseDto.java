package com.swmStrong.demo.domain.user.dto;

import lombok.Builder;

@Builder
public record UserInfoResponseDto(
        String userId,
        String nickname,
        String profileImageUrl,
        int currentStreak,
        int maxStreak,
        int totalSession
) {
}
