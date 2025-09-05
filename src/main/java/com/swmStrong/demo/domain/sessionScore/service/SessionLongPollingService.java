package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.common.response.CustomDeferredResult;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class SessionLongPollingService {
    
    private final SessionScoreService sessionScoreService;
    private final SessionStateManager sessionStateManager;
    private final SessionPollingManager sessionPollingManager;
    
    public SessionLongPollingService(
            SessionScoreService sessionScoreService,
            SessionStateManager sessionStateManager,
            SessionPollingManager sessionPollingManager
    ) {
        this.sessionScoreService = sessionScoreService;
        this.sessionStateManager = sessionStateManager;
        this.sessionPollingManager = sessionPollingManager;
    }
    
    public CustomDeferredResult<List<SessionScoreResponseDto>> getSessionScoreWithLongPolling(
            String userId, LocalDate date
    ) {
        
        String key = String.format("%s:%s", userId, date.toString());
        CustomDeferredResult<List<SessionScoreResponseDto>> deferredResult = 
                new CustomDeferredResult<>(key);
        
        // 먼저 현재 데이터를 확인해서 데이터가 없으면 바로 null 반환
        List<SessionScoreResponseDto> currentData = sessionScoreService.getByUserIdAndSessionDate(userId, date);
        if (currentData.isEmpty()) {
            log.debug("No session data found, returning null immediately: {}", key);
            deferredResult.setResult(List.of());
            return deferredResult;
        }
        
        // 모든 세션이 처리 완료되었는지 확인
        boolean allSessionsProcessed = currentData.stream()
                .allMatch(sessionScore -> sessionStateManager.isSessionProcessed(userId, date, sessionScore.session()));
        
        if (allSessionsProcessed) {
            log.debug("All sessions processed, returning immediately: {}", key);
            deferredResult.setResult(currentData);
            return deferredResult;
        }
        
        // 아직 처리 중인 세션이 있는 경우 대기 큐에 등록
        log.debug("Sessions still processing, registering for long polling: {}", key);
        sessionPollingManager.registerDeferredResult(userId, date, deferredResult);
        
        return deferredResult;
    }
    
}