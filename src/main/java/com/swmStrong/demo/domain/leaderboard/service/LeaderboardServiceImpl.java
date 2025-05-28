package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardRepository;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardRepository leaderboardRepository;
    private final UserInfoProvider userInfoProvider;
    private final CategoryProvider categoryProvider;

    private static final String LEADERBOARD_KEY_PREFIX = "leaderboard:";

    public LeaderboardServiceImpl(
            LeaderboardRepository leaderboardRepository,
            UserInfoProvider userInfoProvider,
            CategoryProvider categoryProvider
    ) {
        this.leaderboardRepository = leaderboardRepository;
        this.userInfoProvider = userInfoProvider;
        this.categoryProvider = categoryProvider;
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
                .nickname(userInfoProvider.getNicknameByUserId(userId))
                .score(leaderboardRepository.findScoreByUserId(key, userId))
                .rank(leaderboardRepository.findRankByUserId(key, userId) + 1)
                .build();
    }

    @Override
    public List<LeaderboardResponseDto> getAllLeaderboard(String category, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateKey(category, date);
        Set<ZSetOperations.TypedTuple<String>> tuples = leaderboardRepository.findAll(key);

        long rank = 1;
        List<LeaderboardResponseDto> response = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String userId = tuple.getValue();
            double score = Optional.ofNullable(tuple.getScore()).orElse(0.0);
            response.add(
                    LeaderboardResponseDto.builder()
                            .userId(userId)
                            //TODO: 미리 닉네임을 레디스에 올려두기
                            .nickname(userInfoProvider.getNicknameByUserId(userId))
                            .score(score)
                            .rank(rank++)
                            .build()
            );
        }
        return response;
    }

    @Override
    public Map<String, List<LeaderboardResponseDto>> getLeaderboards() {
        Map<String, List<LeaderboardResponseDto>> response = new HashMap<>();

        LocalDate date = LocalDate.now();
        List<String> categories = categoryProvider.getCategories();

        for (String category : categories) {
            response.put(category, getLeaderboardPage(category, 1, 10, date));
        }
        return response;
    }

    private String generateKey(String category, LocalDate day) {
        return LEADERBOARD_KEY_PREFIX + category + ":" + day.toString();
    }
}

