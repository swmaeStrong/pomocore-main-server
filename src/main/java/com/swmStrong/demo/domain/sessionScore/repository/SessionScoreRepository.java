package com.swmStrong.demo.domain.sessionScore.repository;

import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SessionScoreRepository extends JpaRepository<SessionScore, Long> {
    SessionScore findByUserIdAndSessionAndSessionDate(String userId, int session, LocalDate sessionDate);
    List<SessionScore> findByUserIdAndSessionDate(String userId, LocalDate sessionDate);
    List<SessionScore> findAllByUserIdAndSessionDate(String userId, LocalDate sessionDate);
}
