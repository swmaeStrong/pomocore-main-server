package com.swmStrong.demo.domain.leaderboard.repository;

import com.swmStrong.demo.domain.leaderboard.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeaderboardRepository extends JpaRepository<Leaderboard, String> {

}
