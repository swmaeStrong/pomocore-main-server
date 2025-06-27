package com.swmStrong.demo.domain.usageLog.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MergedCategoryUsageLogDto(
        String mergedCategory,
        LocalDateTime startTime,
        LocalDateTime endTime,
        String app,
        String title
) {
}
