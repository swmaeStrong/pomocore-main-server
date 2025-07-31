package com.swmStrong.demo.domain.streak.repository;

import com.swmStrong.demo.domain.streak.entity.Streak;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StreakRepository extends JpaRepository<Streak, Long> {
    Optional<Streak> findByUserId(String userId);
}
