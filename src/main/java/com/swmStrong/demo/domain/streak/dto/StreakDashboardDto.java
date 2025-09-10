package com.swmStrong.demo.domain.streak.dto;

import java.util.List;

public record StreakDashboardDto(
        StreakResponseDto streakSummary,
        List<DailyActivityResponseDto> dailyActivities
) {
    public static StreakDashboardDto from(StreakResponseDto streakResponseDto, List<DailyActivityResponseDto> dailyActivities) {
        return new StreakDashboardDto(streakResponseDto, dailyActivities);
    }
}
