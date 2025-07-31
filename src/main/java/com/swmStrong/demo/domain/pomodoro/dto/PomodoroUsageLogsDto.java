package com.swmStrong.demo.domain.pomodoro.dto;

import java.time.LocalDate;
import java.util.List;

public record PomodoroUsageLogsDto(
        LocalDate sessionDate,
        int sessionMinutes,
        List<PomodoroDto> usageLogs
) {
    public record PomodoroDto(
            String title,
            String app,
            String url,
            double duration,
            double timestamp
    ) {}
}