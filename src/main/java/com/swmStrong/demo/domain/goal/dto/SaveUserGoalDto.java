package com.swmStrong.demo.domain.goal.dto;

public record SaveUserGoalDto(
        String category,
        Integer goalValue,
        Integer goalSeconds,
        String period
) {
}
