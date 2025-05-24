package com.swmStrong.demo.domain.leaderboard.repository;


import org.springframework.data.redis.core.ZSetOperations;

import java.util.Set;

public interface LeaderboardRepository {
    void increaseScoreByUserId(String key, String userId, double seconds);
    Set<ZSetOperations.TypedTuple<String>> findPageWithSize(String key, int page, int size);
    Long findRankByUserId(String key, String userId);
    Double findScoreByUserId(String key, String userId);
}
