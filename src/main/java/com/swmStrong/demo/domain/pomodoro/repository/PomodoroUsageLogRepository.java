package com.swmStrong.demo.domain.pomodoro.repository;

import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDate;
import java.util.List;

public interface PomodoroUsageLogRepository extends MongoRepository<PomodoroUsageLog, ObjectId> {
    List<PomodoroUsageLog> findByUserIdAndSessionDate(String userId, LocalDate sessionDate);
    
    default int findMaxSessionByUserIdAndSessionDate(String userId, LocalDate sessionDate) {
        List<PomodoroUsageLog> logs = findByUserIdAndSessionDate(userId, sessionDate);
        return logs.stream()
                .mapToInt(PomodoroUsageLog::getSession)
                .max()
                .orElse(0);
    }
    
    List<PomodoroUsageLog> findByUserIdAndSessionAndSessionDateOrderByTimestamp(String userId, int session, LocalDate sessionDate);
}
