package com.swmStrong.demo.domain.usageLog.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record MergedCategoryUsageLogDto(
        String mergedCategory,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        String app,
        String title
) {
}
