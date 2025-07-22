package com.swmStrong.demo.domain.pomodoro.facade;

import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.repository.PomodoroUsageLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class PomodoroSessionProvider {

    private final PomodoroUsageLogRepository pomodoroUsageLogRepository;

    public PomodoroSessionProvider(PomodoroUsageLogRepository pomodoroUsageLogRepository) {
        this.pomodoroUsageLogRepository = pomodoroUsageLogRepository;
    }

    public List<PomodoroUsageLog> loadByUserIdAndSessionAndSessionDate(String userId, int session, LocalDate sessionDate) {
        return pomodoroUsageLogRepository.findByUserIdAndSessionAndSessionDateOrderByTimestamp(userId, session, sessionDate);
    }
}
