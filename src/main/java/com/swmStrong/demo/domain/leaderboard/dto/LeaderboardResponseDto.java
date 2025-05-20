package com.swmStrong.demo.domain.leaderboard.dto;

import lombok.Builder;

@Builder
public record LeaderboardResponseDto(
        String userId,
        double score,
        long rank
) {
}
