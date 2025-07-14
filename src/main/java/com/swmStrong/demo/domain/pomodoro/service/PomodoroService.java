package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;

public interface PomodoroService {
    void save(String userId, PomodoroUsageLogsDto pomodoroUsageLogsDto);

}
