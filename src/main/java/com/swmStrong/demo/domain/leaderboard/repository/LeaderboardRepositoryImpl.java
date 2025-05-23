package com.swmStrong.demo.domain.leaderboard.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public class LeaderboardRepositoryImpl implements LeaderboardRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public LeaderboardRepositoryImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void increaseScoreByUserId(String key, String userId, double seconds) {
        stringRedisTemplate.opsForZSet().incrementScore(key, userId, seconds);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<String>> getTopUsers(String key, int topN) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, topN-1);
    }

    @Override
    public Long getRankByUserId(String key, String userId) {
        return stringRedisTemplate.opsForZSet().reverseRank(key, userId);
    }

    @Override
    public Double getScoreByUserId(String key, String userId) {
        return stringRedisTemplate.opsForZSet().score(key, userId);
    }
}
