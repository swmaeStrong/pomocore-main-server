package com.swmStrong.demo.domain.sessionScore.facade;

import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import com.swmStrong.demo.domain.user.entity.User;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class SessionScoreProvider {

    private final SessionScoreRepository sessionScoreRepository;

    public SessionScoreProvider(SessionScoreRepository sessionScoreRepository) {
        this.sessionScoreRepository = sessionScoreRepository;
    }

    public int createSession(User user, LocalDate sessionDate, int sessionMinutes) {
        int session = sessionScoreRepository.findMaxSessionByUserIdAndSessionDateWithLock(user.getId(), sessionDate)
                .orElse(0) + 1;
        return sessionScoreRepository.save(SessionScore.builder()
                .user(user)
                .session(session)
                .sessionDate(sessionDate)
                .sessionMinutes(sessionMinutes)
                .build()
        ).getSession();
    }
}
