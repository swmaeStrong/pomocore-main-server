package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
public class SessionStateManager {
    
    private RedisRepository redisRepository;

    public  SessionStateManager(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    private static final String SESSION_PROCESSED_KEY_PREFIX = "session:processed:";
    private static final long TTL = 60 * 10;
    
    public void initializeSessionProcessing(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        redisRepository.setDataWithExpire(key, false, TTL);
        log.debug("Initialized session processing: {}", key);
    }
    
    public void markSessionAsProcessed(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        redisRepository.setDataWithExpire(key, true, TTL);
        log.debug("Marked session as processed: {}", key);
    }
    
    public boolean isSessionProcessed(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        String value = redisRepository.getData(key);
        // null이거나 true면 처리 완료로 간주
        boolean processed = value == null || "true".equals(value);
        log.debug("Session processed check for {}: value={}, processed={}", key, value, processed);
        return processed;
    }
    
    public void removeSessionProcessedFlag(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        redisRepository.deleteData(key);
        log.debug("Removed processed flag for: {}", key);
    }
    
    private String generateKey(String userId, LocalDate date, int session) {
        return SESSION_PROCESSED_KEY_PREFIX + userId + ":" + date.toString() + ":" + session;
    }
}