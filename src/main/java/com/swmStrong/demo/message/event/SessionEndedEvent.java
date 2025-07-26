package com.swmStrong.demo.message.event;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record SessionEndedEvent(
        String userId,
        int session,
        LocalDate sessionDate,
        int sessionMinutes
) {
}
