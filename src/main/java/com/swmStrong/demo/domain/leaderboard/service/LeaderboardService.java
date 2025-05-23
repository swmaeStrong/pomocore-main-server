package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;

import java.util.List;
import java.util.Optional;

public interface LeaderboardService {
    void increaseScore(String category, String userId, double duration);
    List<LeaderboardResponseDto> getTopUsers(String category, int topN);
    Optional<LeaderboardResponseDto> getUserScoreInfo(String category, String userId);
}
