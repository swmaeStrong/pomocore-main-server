package com.swmStrong.demo.domain.leaderboard.dto;

import lombok.Builder;

@Builder
public record LeaderboardResult(
        double score,
        long rank
) {
}