package com.swmStrong.demo.domain.group.dto;

public record SaveGroupGoalDto(
        String category,
        int goalSeconds,
        String period
) {
}
