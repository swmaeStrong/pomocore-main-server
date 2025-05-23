package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface LeaderboardService {
    void increaseScore(String category, String userId, double duration, LocalDateTime timestamp);
    List<LeaderboardResponseDto> getLeaderboardPage(String category, int start, LocalDate date);
    LeaderboardResponseDto getUserScoreInfo(String category, String userId, LocalDate date);
}
