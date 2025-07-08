package com.swmStrong.demo.domain.leaderboard.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record LeaderboardResponseDto(
        String userId,
        String nickname,
        double score,
        long rank,
        List<CategoryDetailDto> details
) {
}
