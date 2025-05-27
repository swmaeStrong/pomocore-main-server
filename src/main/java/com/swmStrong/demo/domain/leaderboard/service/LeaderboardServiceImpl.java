package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardRepository;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserInfoProvider userInfoProvider;

    private static final String LEADERBOARD_KEY_PREFIX = "leaderboard:";

    public LeaderboardServiceImpl(LeaderboardRepository leaderboardRepository, UserInfoProvider userInfoProvider) {
        this.leaderboardRepository = leaderboardRepository;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public void increaseScore(String category, String userId, double duration, LocalDateTime timestamp) {
        LocalDate day = timestamp.toLocalDate();
        String key = generateKey(category, day);
        log.info("Increase score for user {} to {}", userId, duration);
        leaderboardRepository.increaseScoreByUserId(key, userId, duration);
    }

    @Override
    public List<LeaderboardResponseDto> getLeaderboardPage(String category, int page, int size, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateKey(category, date);
        Set<ZSetOperations.TypedTuple<String>> tuples = leaderboardRepository.findPageWithSize(key, page, size);

        List<LeaderboardResponseDto> response = new ArrayList<>();
        long rank = (long) (page-1) * size + 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String userId = tuple.getValue();
            double score = Optional.ofNullable(tuple.getScore()).orElse(0.0);
            response.add(
                    LeaderboardResponseDto.builder()
                            .userId(userId)
                            .nickname(userInfoProvider.getNicknameByUserId(userId))
                            .score(score)
                            .rank(rank++)
                            .build()
            );
        }
        return response;
    }

    @Override
    public LeaderboardResponseDto getUserScoreInfo(String category, String userId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateKey(category, date);

        return LeaderboardResponseDto.builder()
                .userId(userId)
                .score(leaderboardRepository.findScoreByUserId(key, userId))
                .rank(leaderboardRepository.findRankByUserId(key, userId) + 1)
                .build();
    }

    private String generateKey(String category, LocalDate day) {
        return LEADERBOARD_KEY_PREFIX + category + ":" + day.toString();
    }
}

