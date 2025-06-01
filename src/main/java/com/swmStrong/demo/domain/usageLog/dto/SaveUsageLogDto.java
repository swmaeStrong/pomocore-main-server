package com.swmStrong.demo.domain.usageLog.dto;

import java.time.LocalDateTime;

public record SaveUsageLogDto(
        String title,
        String app,
        double duration,
        LocalDateTime timestamp
) {
}
