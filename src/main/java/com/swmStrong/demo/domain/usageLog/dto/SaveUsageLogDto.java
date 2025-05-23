package com.swmStrong.demo.domain.usageLog.dto;

import java.time.LocalDateTime;

public record SaveUsageLogDto(
        String userId,
        String title,
        String app,
        double duration,
        LocalDateTime timestamp
) {
}
