package com.swmStrong.demo.domain.leaderboard.dto;

import lombok.Builder;

@Builder
public record LeaderboardResponseDto(
        String userId,
        String nickname,
        double score,
        long rank
) {
}
