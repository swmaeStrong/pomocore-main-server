package com.swmStrong.demo.domain.pomodoro.repository;

import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PomodoroUsageLogRepository extends MongoRepository<PomodoroUsageLog, ObjectId> {
}
