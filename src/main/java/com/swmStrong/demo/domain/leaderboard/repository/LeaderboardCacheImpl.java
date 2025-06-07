package com.swmStrong.demo.domain.leaderboard.repository;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.Set;

@Repository
public class LeaderboardCacheImpl implements LeaderboardCache {

    private final StringRedisTemplate stringRedisTemplate;

    public LeaderboardCacheImpl(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public void increaseScoreByUserId(String key, String userId, double seconds) {
        stringRedisTemplate.opsForZSet().incrementScore(key, userId, seconds);
    }

    @Override
    public Set<ZSetOperations.TypedTuple<String>> findPageWithSize(String key, int page, int size) {
        Set<ZSetOperations.TypedTuple<String>> result =
                stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, (long) (page - 1) * size, (long) page * size - 1);
        return result != null ? result : Collections.emptySet();
    }

    @Override
    public Long findRankByUserId(String key, String userId) {
        Long rank = stringRedisTemplate.opsForZSet().reverseRank(key, userId);
        return rank != null ? rank : -1L;
    }

    @Override
    public Double findScoreByUserId(String key, String userId) {
        Double score = stringRedisTemplate.opsForZSet().score(key, userId);
        return score != null ? score : 0.0;
    }

    @Override
    public Set<ZSetOperations.TypedTuple<String>> findAll(String key) {
        Set<ZSetOperations.TypedTuple<String>> result = stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
        return result != null ? result : Collections.emptySet();
    }
}