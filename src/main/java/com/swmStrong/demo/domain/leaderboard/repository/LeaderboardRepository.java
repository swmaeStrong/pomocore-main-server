package com.swmStrong.demo.domain.leaderboard.repository;


import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

public interface LeaderboardRepository {
    void increaseScoreByUserId(String key, String userId, long seconds);
    Set<ZSetOperations.TypedTuple<String>> getTopUsers(String key, int topN);
    Long getRankByUserId(String key, String userId);
    Double getScoreByUserId(String key, String userId);
}
