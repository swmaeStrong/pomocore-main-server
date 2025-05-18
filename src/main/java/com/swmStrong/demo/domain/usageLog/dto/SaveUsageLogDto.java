package com.swmStrong.demo.domain.usageLog.dto;

import java.time.Duration;
import java.time.LocalDateTime;

public record SaveUsageLogDto(
        String userId,
        String title,
        String app,
        Duration duration,
        LocalDateTime timestamp
) {
}
