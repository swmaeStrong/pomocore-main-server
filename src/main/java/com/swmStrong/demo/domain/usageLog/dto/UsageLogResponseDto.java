package com.swmStrong.demo.domain.usageLog.dto;

import com.swmStrong.demo.domain.usageLog.entity.UsageLog;

import java.time.Duration;
import java.time.LocalDateTime;

public record UsageLogResponseDto(
        String userId,
        String title,
        String app,
        Duration duration,
        LocalDateTime timestamp
) {
    public static UsageLogResponseDto from(UsageLog usageLog) {
        return new UsageLogResponseDto(
                usageLog.getUserId(),
                usageLog.getTitle(),
                usageLog.getApp(),
                usageLog.getDuration(),
                usageLog.getTimestamp()
        );
    }
}
