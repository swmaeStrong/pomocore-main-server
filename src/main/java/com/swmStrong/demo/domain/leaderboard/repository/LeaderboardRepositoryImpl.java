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
    public Set<ZSetOperations.TypedTuple<String>> findPageWithSize(String key, int page, int size) {
        return stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, (long) (page-1) * size, (long) page * size-1);
    }

    @Override
    public Long findRankByUserId(String key, String userId) {
        return stringRedisTemplate.opsForZSet().reverseRank(key, userId);
    }

    @Override
    public Double findScoreByUserId(String key, String userId) {
        return stringRedisTemplate.opsForZSet().score(key, userId);
    }
}
