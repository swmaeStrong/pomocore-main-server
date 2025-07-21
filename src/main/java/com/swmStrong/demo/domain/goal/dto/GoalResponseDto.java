package com.swmStrong.demo.domain.goal.dto;

public record GoalResponseDto(
        String category,
        int currentSeconds,
        int goalSeconds
) {

}
