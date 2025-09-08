package com.swmStrong.demo.domain.pomodoro.dto;

import java.util.List;

public record AppUsageDto(
        double totalSeconds,
        List<AppUsageResult> distractedAppUsage,
        List<AppUsageResult> workAppUsage
) {
    public static AppUsageDto from(double totalSeconds, List<AppUsageResult> distractedAppUsage, List<AppUsageResult> workAppUsage) {
        return new AppUsageDto(totalSeconds, distractedAppUsage, workAppUsage);
    }
}
