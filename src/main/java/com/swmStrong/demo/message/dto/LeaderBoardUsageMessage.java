package com.swmStrong.demo.message.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record LeaderBoardUsageMessage(
        String userId,
        String category,
        long duration,
        LocalDateTime timestamp
) {
}
