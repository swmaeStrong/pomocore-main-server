package com.swmStrong.demo.domain.pomodoro.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record UsageLogDetailDto(
        int totalDistractedDuration,
        int scoreByDistractedDuration,
        List<AppUsageResult> details
) {
}

