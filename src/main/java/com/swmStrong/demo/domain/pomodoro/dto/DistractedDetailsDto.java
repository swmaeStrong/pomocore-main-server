package com.swmStrong.demo.domain.pomodoro.dto;

import lombok.Builder;

@Builder
public record DistractedDetailsDto(
        String distractedApp,
        double duration,
        int count
) {
}
