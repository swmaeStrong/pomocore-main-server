package com.swmStrong.demo.domain.sessionScore.facade;

import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SessionNumberProvider {

    private final SessionScoreRepository sessionScoreRepository;

    public SessionNumberProvider(SessionScoreRepository sessionScoreRepository) {
        this.sessionScoreRepository = sessionScoreRepository;
    }

    public int getNextSessionByUserIDAndSessionDate(String userId, LocalDate date) {
        return sessionScoreRepository.findMaxSessionByUserIdAndSessionDate(userId, date)
                .orElse(0) + 1;
    }
}
