package com.swmStrong.demo.domain.usageLog.dto;

public record CategoryUsageDto(
        String category,
        long duration,
        String color
) {
}
