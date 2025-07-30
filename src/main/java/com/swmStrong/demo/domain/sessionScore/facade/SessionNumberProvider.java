package com.swmStrong.demo.domain.sessionScore.facade;

import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionNumberProvider {

    private final SessionScoreRepository sessionScoreRepository;
    private final ConcurrentHashMap<String, Object> userLocks = new ConcurrentHashMap<>();

    public SessionNumberProvider(SessionScoreRepository sessionScoreRepository) {
        this.sessionScoreRepository = sessionScoreRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int getNextSessionByUserIDAndSessionDate(String userId, LocalDate date) {
        Object lock = userLocks.computeIfAbsent(userId, k -> new Object());
        synchronized (lock) {
            return sessionScoreRepository.findMaxSessionByUserIdAndSessionDateWithLock(userId, date)
                    .orElse(0) + 1;
        }
    }
}
