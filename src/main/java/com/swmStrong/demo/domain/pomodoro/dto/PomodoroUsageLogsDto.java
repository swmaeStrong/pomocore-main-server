package com.swmStrong.demo.domain.pomodoro.dto;

import java.time.LocalDate;
import java.util.List;

public record PomodoroUsageLogsDto(
        LocalDate sessionDate,
        int session,
        int sessionMinutes,
        boolean isEnd,
        List<PomodoroDto> usageLogs
) {
    public record PomodoroDto(
            String title,
            String app,
            String url,
            String category,
            double duration,
            double timestamp
    ) {}
}