package com.swmStrong.demo.domain.pomodoro.dto;

import lombok.Builder;

@Builder
public record AppUsageResult(
        String app,
        double duration,
        int count
) {
}
