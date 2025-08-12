package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.common.response.CustomDeferredResult;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class SessionPollingManager {
    
    private final Map<String, CustomDeferredResult<List<SessionScoreResponseDto>>> waitingRequests = new ConcurrentHashMap<>();
    
    public void registerDeferredResult(String userId, LocalDate date, int session, 
                                       CustomDeferredResult<List<SessionScoreResponseDto>> deferredResult) {
        String key = generateKey(userId, date, session);
        
        deferredResult.onCompletion(() -> {
            log.debug("Long polling completed for key: {}", key);
            waitingRequests.remove(key);
        });
        
        deferredResult.onError(throwable -> {
            log.error("Long polling error for key: {}", key, throwable);
            waitingRequests.remove(key);
        });
        
        waitingRequests.put(key, deferredResult);
        log.debug("Registered long polling request for key: {}", key);
    }
    
    public void registerDeferredResult(String userId, LocalDate date, 
                                       CustomDeferredResult<List<SessionScoreResponseDto>> deferredResult) {
        String key = generateKey(userId, date);
        
        deferredResult.onCompletion(() -> {
            log.debug("Long polling completed for key: {}", key);
            waitingRequests.remove(key);
        });
        
        deferredResult.onError(throwable -> {
            log.error("Long polling error for key: {}", key, throwable);
            waitingRequests.remove(key);
        });
        
        waitingRequests.put(key, deferredResult);
        log.debug("Registered long polling request for key: {}", key);
    }
    
    public void notifySessionProcessed(String userId, LocalDate date, int session, List<SessionScoreResponseDto> data) {
        String sessionKey = generateKey(userId, date, session);
        String dateKey = generateKey(userId, date);
        
        // 개별 세션 키에 대한 알림 (기존 호환성)
        CustomDeferredResult<List<SessionScoreResponseDto>> sessionDeferredResult = waitingRequests.get(sessionKey);
        if (sessionDeferredResult != null) {
            boolean success = sessionDeferredResult.setResult(data);
            if (success) {
                log.debug("Notified waiting request for session key: {}", sessionKey);
                waitingRequests.remove(sessionKey);
            } else {
                log.warn("Failed to set result for session key: {} (already set or expired)", sessionKey);
            }
        } else {
            log.debug("No waiting request found for session key: {}", sessionKey);
        }
        
    }
    
    public void notifyAllSessionsProcessed(String userId, LocalDate date, List<SessionScoreResponseDto> data) {
        String dateKey = generateKey(userId, date);
        
        CustomDeferredResult<List<SessionScoreResponseDto>> dateDeferredResult = waitingRequests.get(dateKey);
        if (dateDeferredResult != null) {
            boolean success = dateDeferredResult.setResult(data);
            if (success) {
                log.debug("Notified all sessions processed for date key: {}", dateKey);
                waitingRequests.remove(dateKey);
            } else {
                log.warn("Failed to set result for date key: {} (already set or expired)", dateKey);
            }
        } else {
            log.debug("No waiting request found for date key: {}", dateKey);
        }
    }
    
    public boolean hasWaitingRequest(String userId, LocalDate date, int session) {
        String key = generateKey(userId, date, session);
        return waitingRequests.containsKey(key);
    }
    
    private String generateKey(String userId, LocalDate date, int session) {
        return String.format("%s:%s:%d", userId, date.toString(), session);
    }
    
    private String generateKey(String userId, LocalDate date) {
        return String.format("%s:%s", userId, date.toString());
    }
    
    public int getWaitingRequestCount() {
        return waitingRequests.size();
    }
}