package com.swmStrong.demo.domain.leaderboard.facade;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardCache;
import com.swmStrong.demo.domain.leaderboard.service.LeaderboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class LeaderboardProvider {

    private final LeaderboardCache leaderboardCache;
    private final LeaderboardService leaderboardService;

    public LeaderboardProvider(LeaderboardCache leaderboardCache,  LeaderboardService leaderboardService) {
        this.leaderboardCache = leaderboardCache;
        this.leaderboardService = leaderboardService;
    }

    public double getUserScore(String userId, String category, LocalDate date, PeriodType periodType) {
        String key = leaderboardService.generateKey(category, date, periodType);
        return leaderboardCache.findScoreByUserId(key, userId);
    }
}
