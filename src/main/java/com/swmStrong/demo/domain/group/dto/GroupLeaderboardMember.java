package com.swmStrong.demo.domain.group.dto;

import com.swmStrong.demo.domain.leaderboard.dto.CategoryDetailDto;
import lombok.Builder;

import java.util.List;

@Builder
public record GroupLeaderboardMember(
        String userId,
        String nickname,
        double score,
        long rank,
        String profileImageUrl,
        List<CategoryDetailDto> details
) {
}