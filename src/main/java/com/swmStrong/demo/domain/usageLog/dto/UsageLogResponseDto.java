package com.swmStrong.demo.domain.usageLog.dto;

import com.swmStrong.demo.domain.usageLog.entity.UsageLog;

public record UsageLogResponseDto(
        String userId,
        String title,
        String app,
        String url,
        double duration,
        double timestamp
) {
    public static UsageLogResponseDto from(UsageLog usageLog) {
        return new UsageLogResponseDto(
                usageLog.getUserId(),
                usageLog.getTitle(),
                usageLog.getApp(),
                usageLog.getUrl(),
                usageLog.getDuration(),
                usageLog.getTimestamp()
        );
    }
}
