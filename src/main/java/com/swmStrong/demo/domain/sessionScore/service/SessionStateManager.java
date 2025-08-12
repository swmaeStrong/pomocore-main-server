package com.swmStrong.demo.domain.sessionScore.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
public class SessionStateManager {
    
    private static final String SESSION_PROCESSED_KEY_PREFIX = "session:processed:";
    private static final Duration TTL = Duration.ofHours(24); // 24시간 후 자동 삭제
    
    private final StringRedisTemplate redisTemplate;
    
    public SessionStateManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public void markSessionAsProcessed(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        redisTemplate.opsForValue().set(key, "true", TTL);
        log.debug("Marked session as processed: {}", key);
    }
    
    public boolean isSessionProcessed(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        String value = redisTemplate.opsForValue().get(key);
        boolean processed = "true".equals(value);
        log.debug("Session processed check for {}: {}", key, processed);
        return processed;
    }
    
    public void removeSessionProcessedFlag(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        redisTemplate.delete(key);
        log.debug("Removed processed flag for: {}", key);
    }
    
    private String generateKey(String userId, LocalDate date, int session) {
        return SESSION_PROCESSED_KEY_PREFIX + userId + ":" + date.toString() + ":" + session;
    }
}