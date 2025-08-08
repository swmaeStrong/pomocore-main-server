package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.message.dto.LeaderBoardUsageMessage;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface LeaderboardService {
    void increaseSessionCount(String userId, LocalDate date);
    void increaseScoreBatch(List<LeaderBoardUsageMessage> messages);
    List<LeaderboardResponseDto> getLeaderboardPage(String category, int page, int size, LocalDate date, PeriodType periodType);
    LeaderboardResponseDto getUserScoreInfo(String category, String userId, LocalDate date, PeriodType periodType);
    Map<String, List<LeaderboardResponseDto>> getLeaderboards();
    String generateKey(String category, LocalDate date, PeriodType periodType);
}
