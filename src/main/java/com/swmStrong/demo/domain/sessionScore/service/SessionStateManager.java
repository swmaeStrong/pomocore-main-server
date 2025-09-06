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
    private final RedisRepository redisRepository;

    public  SessionStateManager(RedisRepository redisRepository) {
        this.redisRepository = redisRepository;
    }

    private static final String SESSION_PROCESSED_KEY_PREFIX = "session:processed:";
    private static final long TTL = 60 * 10;
    
    public void initializeSessionProcessing(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        redisRepository.setDataWithExpire(key, 0, TTL);
        log.debug("Initialized session processing: {}", key);
    }
    
    public void markSessionAsProcessed(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        redisRepository.deleteData(key);
        log.debug("Marked session as processed: {}", key);
    }
    
    public boolean isSessionProcessed(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        String value = redisRepository.getData(key);

        boolean processed = value == null;
        log.debug("Session processed check for {}: value={}, processed={}", key, value, processed);
        return processed;
    }
    
    private String generateKey(String userId, LocalDate date, int session) {
        return SESSION_PROCESSED_KEY_PREFIX + userId + ":" + date.toString() + ":" + session;
    }
}