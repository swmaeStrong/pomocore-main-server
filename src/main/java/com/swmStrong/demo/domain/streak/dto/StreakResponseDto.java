package com.swmStrong.demo.domain.streak.dto;

import com.swmStrong.demo.domain.streak.entity.Streak;

public record StreakResponseDto(
        int currentStreak,
        int maxStreak
) {
    public static StreakResponseDto of(Streak streak) {
        return new StreakResponseDto(
                streak.getCurrentStreak(),
                streak.getMaxStreak()
        );
    }
}
