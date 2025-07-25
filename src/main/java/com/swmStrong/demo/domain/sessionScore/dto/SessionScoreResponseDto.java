package com.swmStrong.demo.domain.sessionScore.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SessionScoreResponseDto(
    String title,
    int session,
    LocalDate sessionDate,
    double timestamp,
    double duration,
    int score,
    List<SessionDetailDto> details
) {
    public record SessionDetailDto(
            String category,
            String categoryDetail,
            double timestamp,
            double duration
    ) {}
}
