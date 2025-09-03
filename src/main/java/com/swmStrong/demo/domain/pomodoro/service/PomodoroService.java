package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.domain.pomodoro.dto.AppUsageDto;
import com.swmStrong.demo.domain.pomodoro.dto.AppUsageResult;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.pomodoro.dto.CategoryUsageDto;

import java.time.LocalDate;
import java.util.List;

public interface PomodoroService {
    void save(String userId, PomodoroUsageLogsDto pomodoroUsageLogsDto);
    List<CategoryUsageDto> getUsageLogByUserIdAndDateBetween(String userId, LocalDate date);
    AppUsageDto getDetailsByUserIdAndSessionDateAndSession(String userId, LocalDate date, int session);
}
