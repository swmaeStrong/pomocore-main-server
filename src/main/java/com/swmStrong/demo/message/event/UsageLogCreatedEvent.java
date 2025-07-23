package com.swmStrong.demo.message.event;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record UsageLogCreatedEvent(
        String userId,
        LocalDate activityDate
) {
}
