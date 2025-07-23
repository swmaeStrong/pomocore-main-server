package com.swmStrong.demo.domain.streak.dto;

import com.swmStrong.demo.domain.streak.entity.DailyActivity;

import java.time.LocalDate;

public record DailyActivityResponseDto(
        LocalDate date,
        int activityCount
) {
    public static DailyActivityResponseDto of(DailyActivity dailyActivity) {
        return new DailyActivityResponseDto(
                dailyActivity.getActivityDate(),
                dailyActivity.getActivityCount()
        );
    }
}
