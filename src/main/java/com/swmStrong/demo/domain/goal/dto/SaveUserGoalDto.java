package com.swmStrong.demo.domain.goal.dto;

public record SaveUserGoalDto(
        String category,
        int goalValue,
        String period
) {
}
