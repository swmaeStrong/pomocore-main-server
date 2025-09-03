package com.swmStrong.demo.domain.pomodoro.dto;

import java.util.List;

public record AppUsageDto(
        List<AppUsageResult> distractedAppUsage,
        List<AppUsageResult> workAppUsage
) {
    public static AppUsageDto from(List<AppUsageResult> distractedAppUsage, List<AppUsageResult> workAppUsage) {
        return new AppUsageDto(distractedAppUsage, workAppUsage);
    }
}
