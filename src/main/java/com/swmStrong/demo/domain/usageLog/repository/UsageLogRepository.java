package com.swmStrong.demo.domain.usageLog.repository;

import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface UsageLogRepository extends MongoRepository<UsageLog, String> {
    List<UsageLog> findByUserId(String userId);
    List<UsageLog> findByUserIdAndTimestampBetween(String userId, LocalDateTime start, LocalDateTime end);
}
