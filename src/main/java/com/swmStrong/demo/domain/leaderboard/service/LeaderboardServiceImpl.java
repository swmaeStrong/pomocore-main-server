package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;

    private static final String LEADERBOARD_KEY_PREFIX = "leaderboard:";

    public LeaderboardServiceImpl(LeaderboardRepository leaderboardRepository) {
        this.leaderboardRepository = leaderboardRepository;
    }

    @Override
    public void increaseScore(String category, String userId, long duration) {
        String key = generateKey(category);
        log.info("Increase score for user {} to {}", userId, duration);
        leaderboardRepository.increaseScoreByUserId(key, userId, duration);
    }

    @Override
    public List<LeaderboardResponseDto> getTopUsers(String category, int topN) {
        String key = generateKey(category);
        Set<ZSetOperations.TypedTuple<String>> tuples = leaderboardRepository.getTopUsers(key, topN);

        List<LeaderboardResponseDto> response = new ArrayList<>();
        long rank = 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String userId = tuple.getValue();
            double score = Optional.ofNullable(tuple.getScore()).orElse(0.0);
            response.add(
                    LeaderboardResponseDto.builder()
                            .userId(userId)
                            .score(score)
                            .rank(rank++)
                            .build()
            );
        }
        return response;
    }

    @Override
    public Optional<LeaderboardResponseDto> getUserScoreInfo(String category, String userId) {
        String key = generateKey(category);
        Long rank = leaderboardRepository.getRankByUserId(key, userId);
        Double score = leaderboardRepository.getScoreByUserId(key, userId);


        if (rank == null || score == null) return Optional.empty();
        return Optional.of(
                LeaderboardResponseDto.builder()
                        .userId(userId)
                        .rank(rank)
                        .score(score)
                        .build()
        );
    }

    private String generateKey(String category) {
        return LEADERBOARD_KEY_PREFIX + category;
    }
}

