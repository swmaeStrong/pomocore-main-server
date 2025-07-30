package com.swmStrong.demo.domain.sessionScore.repository;

import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SessionScoreRepository extends JpaRepository<SessionScore, Long> {
    SessionScore findByUserIdAndSessionAndSessionDate(String userId, int session, LocalDate sessionDate);
    List<SessionScore> findByUserIdAndSessionDate(String userId, LocalDate sessionDate);
    List<SessionScore> findAllByUserIdAndSessionDate(String userId, LocalDate sessionDate);
    
    @Query("SELECT MAX(s.session) FROM SessionScore s WHERE s.user.id = :userId AND s.sessionDate = :sessionDate")
    Optional<Integer> findMaxSessionByUserIdAndSessionDate(@Param("userId") String userId, @Param("sessionDate") LocalDate sessionDate);
    
    @Query(value = "SELECT s.session FROM session_score s WHERE s.user_id = :userId AND s.session_date = :sessionDate ORDER BY s.session DESC LIMIT 1 FOR UPDATE", nativeQuery = true)
    Optional<Integer> findMaxSessionByUserIdAndSessionDateWithLock(@Param("userId") String userId, @Param("sessionDate") LocalDate sessionDate);
}
