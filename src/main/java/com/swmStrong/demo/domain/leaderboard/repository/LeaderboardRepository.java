package com.swmStrong.demo.domain.leaderboard.repository;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.leaderboard.entity.Leaderboard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeaderboardRepository extends JpaRepository<Leaderboard, String> {
    List<Leaderboard> findByCategoryIdAndPeriodTypeAndPeriodKeyAndUserIdInOrderByScoreDesc(
            String categoryId, PeriodType periodType, String periodKey, List<String> userIds
    );
}
