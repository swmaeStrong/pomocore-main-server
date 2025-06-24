package com.swmStrong.demo.domain.usageLog.dto;

public record SaveUsageLogDto(
        String title,
        String app,
        String url,
        double duration,
        double timestamp
) {
}
