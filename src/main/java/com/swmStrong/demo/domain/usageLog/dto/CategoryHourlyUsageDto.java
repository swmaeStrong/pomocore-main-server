package com.swmStrong.demo.domain.usageLog.dto;

import java.time.LocalDateTime;

public record CategoryHourlyUsageDto(
        LocalDateTime hour,
        String category,
        String color,
        double totalDuration
) {
}
