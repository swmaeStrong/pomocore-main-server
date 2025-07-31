package com.swmStrong.demo.domain.pomodoro.dto;

public record SessionResponseDto(
        double timestamp,
        double duration,
        String category
) {

}
