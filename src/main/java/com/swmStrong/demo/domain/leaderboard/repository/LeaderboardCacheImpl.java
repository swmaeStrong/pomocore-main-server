package com.swmStrong.demo.domain.leaderboard.repository;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResult;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Map<String, Double> findScoresByUserIds(String key, List<String> userIds) {
        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String userId : userIds) {
                connection.zSetCommands().zScore(key.getBytes(), userId.getBytes());
            }
            return null;
        });

        Map<String, Double> scoreMap = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            String userId = userIds.get(i);
            Double score = (Double) results.get(i);
            scoreMap.put(userId, score != null ? score : 0.0);
        }
        
        return scoreMap;
    }

    @Override
    public Map<String, LeaderboardResult> findResultsByUserIds(String key, List<String> userIds) {
        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            for (String userId : userIds) {
                connection.zSetCommands().zScore(key.getBytes(), userId.getBytes());
                connection.zSetCommands().zRevRank(key.getBytes(), userId.getBytes());
            }
            return null;
        });

        Map<String, LeaderboardResult> resultMap = new HashMap<>();
        for (int i = 0; i < userIds.size(); i++) {
            String userId = userIds.get(i);
            Double score = (Double) results.get(i * 2);
            Long rank = (Long) results.get(i * 2 + 1);
            
            resultMap.put(userId, LeaderboardResult.builder()
                    .score(score != null ? score : 0.0)
                    .rank(rank != null ? rank + 1 : 0) // Redis rank는 0부터 시작하므로 +1
                    .build());
        }
        
        return resultMap;
    }

    @Override
    public Set<ZSetOperations.TypedTuple<String>> findAll(String key) {
        Set<ZSetOperations.TypedTuple<String>> result = stringRedisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
        return result != null ? result : Collections.emptySet();
    }
}