package com.swmStrong.demo.domain.pomodoro.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record PomodoroResponseDto(
        LocalDate sessionDate,
        int session,
        int sessionMinutes,
        double workTime,
        double breakTime,
        double afkTime,
        List<SessionResponseDto> usageLogs
) {
}
