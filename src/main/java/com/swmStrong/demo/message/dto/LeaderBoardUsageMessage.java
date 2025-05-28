package com.swmStrong.demo.message.dto;

import lombok.Builder;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public record LeaderBoardUsageMessage(
        String userId,
        String categoryId,
        double duration,
        LocalDateTime timestamp
) {
    @Builder
    public LeaderBoardUsageMessage(String userId, ObjectId categoryId, double duration, LocalDateTime timestamp) {
        this(
                userId,
                categoryId != null ? categoryId.toHexString() : null,
                duration,
                timestamp
        );
    }
}
