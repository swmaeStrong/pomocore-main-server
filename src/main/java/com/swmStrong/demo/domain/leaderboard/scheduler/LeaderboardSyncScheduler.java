package com.swmStrong.demo.domain.leaderboard.scheduler;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.leaderboard.entity.Leaderboard;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.swmStrong.demo.domain.leaderboard.service.LeaderboardServiceImpl.LEADERBOARD_KEY_PREFIX;

@Service
public class LeaderboardSyncScheduler {

    private final StringRedisTemplate stringRedisTemplate;
    private final LeaderboardRepository leaderboardRepository;

    public LeaderboardSyncScheduler(
            StringRedisTemplate stringRedisTemplate,
            LeaderboardRepository leaderboardRepository
    ) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.leaderboardRepository = leaderboardRepository;
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void syncLeaderboard() {
        Set<String> keys = stringRedisTemplate.keys(LEADERBOARD_KEY_PREFIX+":*");
        if (keys.isEmpty()) {
            return;
        }

        List<Leaderboard> leaderboards = new ArrayList<>();
        for (String key: keys) {
            String[] parts = key.split(":");
            String categoryId = parts[1];
            String periodKey = parts[2];
            PeriodType periodType = resolvePeriodType(periodKey);

            Set<ZSetOperations.TypedTuple<String>> rankings = stringRedisTemplate.opsForZSet().rangeWithScores(key, 0, -1);

            stringRedisTemplate.delete(key);

            if (rankings == null || rankings.isEmpty()) {
                continue;
            }

            int rank = 1;
            for (ZSetOperations.TypedTuple<String> ranking : rankings) {
                String userId = ranking.getValue();
                double score = Optional.ofNullable(ranking.getScore()).orElse(0.0);

                leaderboards.add(
                        Leaderboard.builder()
                                .userId(userId)
                                .categoryId(categoryId)
                                .periodType(periodType)
                                .periodKey(periodKey)
                                .rank(rank++)
                                .score(score)
                                .build()
                );
            }
        }
        leaderboardRepository.saveAll(leaderboards);
    }

    private PeriodType resolvePeriodType(String periodKey) {
        if (periodKey.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return PeriodType.DAILY;
        } else if (periodKey.contains("W")) {
            return PeriodType.WEEKLY;
        } else if (periodKey.contains("M")) {
            return PeriodType.MONTHLY;
        }
        throw new ApiException(ErrorCode.PERIOD_KEY_ERROR);
    }
}
