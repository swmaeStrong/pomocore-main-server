package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.domain.pomodoro.dto.PomodoroResponseDto;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;

import java.time.LocalDate;
import java.util.List;

public interface PomodoroService {
    void save(String userId, PomodoroUsageLogsDto pomodoroUsageLogsDto);
    List<PomodoroResponseDto> getPomodoroSessionResult(String userId, LocalDate date);
}
