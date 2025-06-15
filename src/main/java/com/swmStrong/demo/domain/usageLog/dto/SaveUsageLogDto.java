package com.swmStrong.demo.domain.usageLog.dto;

import java.time.LocalDateTime;

public record SaveUsageLogDto(
        String title,
        String app,
        String domain,
        double duration,
        LocalDateTime timestamp
) {
}
