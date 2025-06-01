package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface LeaderboardService {
    void increaseScore(String categoryId, String userId, double duration, LocalDateTime timestamp);
    List<LeaderboardResponseDto> getLeaderboardPageDaily(String category, int page, int size, LocalDate date);
    List<LeaderboardResponseDto> getLeaderboardPageWeekly(String category, int page, int size, LocalDate date);
    List<LeaderboardResponseDto> getLeaderboardPageMonthly(String category, int page, int size, LocalDate date);
    LeaderboardResponseDto getUserScoreInfo(String category, String userId, LocalDate date);
    List<LeaderboardResponseDto> getAllLeaderboard(String category, LocalDate date);
    Map<String, List<LeaderboardResponseDto>> getLeaderboards();
}
