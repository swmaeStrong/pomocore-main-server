package com.swmStrong.demo.domain.pomodoro.dto;

import lombok.Builder;

@Builder
public record CategoryUsageDto(
        String category,
        double duration
) {
}
