package com.swmStrong.demo.domain.usageLog.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CategorizedUsageLogDto(
        LocalDateTime timestamp,
        String category,
        String app,
        String title,
        String url
) {
}
