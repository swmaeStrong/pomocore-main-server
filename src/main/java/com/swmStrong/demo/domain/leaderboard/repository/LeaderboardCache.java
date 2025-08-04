package com.swmStrong.demo.domain.leaderboard.repository;


import org.springframework.data.redis.core.ZSetOperations;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResult;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LeaderboardCache {
    void increaseScoreByUserId(String key, String userId, double seconds);
    Set<ZSetOperations.TypedTuple<String>> findPageWithSize(String key, int page, int size);
    Long findRankByUserId(String key, String userId);
    Double findScoreByUserId(String key, String userId);
    Map<String, Double> findScoresByUserIds(String key, List<String> userIds);
    Map<String, LeaderboardResult> findResultsByUserIds(String key, List<String> userIds);
    Set<ZSetOperations.TypedTuple<String>> findAll(String key);
}
