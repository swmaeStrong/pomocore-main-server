package com.swmStrong.demo.domain.leaderboard.dto;

import lombok.Builder;

@Builder
public record CategoryDetailDto(
        String category,
        double score
) {
}