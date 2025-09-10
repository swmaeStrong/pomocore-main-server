package com.swmStrong.demo.domain.pomodoro.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

public record AppUsageDto(
        double totalSeconds,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        List<DailyUsageResult> dailyResults,
        List<AppUsageResult> distractedAppUsage,
        List<AppUsageResult> workAppUsage
) {
    public static AppUsageDto from(
            double totalSeconds, List<DailyUsageResult> dailyResultList,
            List<AppUsageResult> distractedAppUsage, List<AppUsageResult> workAppUsage
    ) {
        return new AppUsageDto(totalSeconds, dailyResultList, distractedAppUsage, workAppUsage);
    }

    public static AppUsageDto from(
            double totalSeconds,
            List<AppUsageResult> distractedAppUsage, List<AppUsageResult> workAppUsage
    ) {
        return new AppUsageDto(totalSeconds, null, distractedAppUsage, workAppUsage);
    }

    @Getter
    public static class DailyUsageResult {
        LocalDate date;
        double workSeconds;
        double distractedSeconds;

        public DailyUsageResult(LocalDate date) {
            this.date = date;
            this.workSeconds = 0;
            this.distractedSeconds = 0;
        }

        public void increaseWorkSeconds(double workSeconds) {
            this.workSeconds += workSeconds;
        }

        public void increaseDistractedSeconds(double distractedSeconds) {
            this.distractedSeconds += distractedSeconds;
        }

    }
}
