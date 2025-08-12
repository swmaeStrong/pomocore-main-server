package com.swmStrong.demo.message.event;

import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record SessionProcessedEvent(
        String userId,
        int session,
        LocalDate sessionDate,
        List<SessionScoreResponseDto> sessionScores
) {
}