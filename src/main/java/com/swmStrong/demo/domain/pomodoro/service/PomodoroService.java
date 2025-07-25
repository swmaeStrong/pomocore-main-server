package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.domain.pomodoro.dto.PomodoroResponseDto;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;

import java.time.LocalDate;
import java.util.List;

public interface PomodoroService {
    void save(String userId, PomodoroUsageLogsDto pomodoroUsageLogsDto);
    List<CategoryUsageDto> getUsageLogByUserIdAndDateBetween(String userId, LocalDate date);
}
