package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardRepository;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
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
    public void increaseScore(String categoryId, String userId, double duration, LocalDateTime timestamp) {
        LocalDate day = timestamp.toLocalDate();
        ObjectId categoryObjectId = new ObjectId(categoryId);
        String category = categoryProvider.getCategoryById(categoryObjectId);

        String dailyKey = generateDailyKey(category, day);
        log.info("Increase score for user {} to {} for daily", userId, duration);
        leaderboardRepository.increaseScoreByUserId(dailyKey, userId, duration);
        String weeklyKey = generateWeeklyKey(category, day);
        log.info("Increase score for user {} to {} for weekly", userId, duration);
        leaderboardRepository.increaseScoreByUserId(weeklyKey, userId, duration);
        String monthlyKey = generateMonthlyKey(category, day);
        log.info("Increase score for user {} to {} for monthly", userId, duration);
        leaderboardRepository.increaseScoreByUserId(monthlyKey, userId, duration);
    }

    @Override
    public List<LeaderboardResponseDto> getLeaderboardPageDaily(String category, int page, int size, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateDailyKey(category, date);
        return getPageByKey(key, page, size);
    }


    @Override
    public List<LeaderboardResponseDto> getLeaderboardPageWeekly(String category, int page, int size, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateWeeklyKey(category, date);
        return getPageByKey(key, page, size);
    }

    @Override
    public List<LeaderboardResponseDto> getLeaderboardPageMonthly(String category, int page, int size, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        String key = generateMonthlyKey(category, date);
        return getPageByKey(key, page, size);
    }

    @Override
    public LeaderboardResponseDto getUserScoreInfo(String category, String userId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateDailyKey(category, date);

        return LeaderboardResponseDto.builder()
                .userId(userId)
                .nickname(userInfoProvider.loadNicknameByUserId(userId))
                .score(leaderboardRepository.findScoreByUserId(key, userId))
                .rank(leaderboardRepository.findRankByUserId(key, userId) + 1)
                .build();
    }

    @Override
    public List<LeaderboardResponseDto> getAllLeaderboard(String category, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateDailyKey(category, date);
        Set<ZSetOperations.TypedTuple<String>> tuples = leaderboardRepository.findAll(key);

        Map<String, String> userNicknames = userInfoProvider.loadNicknamesByUserIds(
                tuples.stream()
                        .map(ZSetOperations.TypedTuple::getValue)
                        .toList()
        );

        long rank = 1;
        List<LeaderboardResponseDto> response = new ArrayList<>();
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String userId = tuple.getValue();
            double score = Optional.ofNullable(tuple.getScore()).orElse(0.0);
            response.add(
                    LeaderboardResponseDto.builder()
                            .userId(userId)
                            .nickname(userNicknames.getOrDefault(userId, "Unknown"))
                            .score(score)
                            .rank(rank++)
                            .build()
            );
        }
        return response;
    }

    @Override
    public Map<String, List<LeaderboardResponseDto>> getLeaderboards() {
        Map<String, List<LeaderboardResponseDto>> response = new LinkedHashMap<>();

        LocalDate date = LocalDate.now();
        List<String> categories = categoryProvider.getCategories();

        for (String category : categories) {
            response.put(category, getLeaderboardPageDaily(category, 1, 10, date));
        }
        return response;
    }

    private String generateDailyKey(String category, LocalDate day) {
        return String.format("%s:%s:%s", LEADERBOARD_KEY_PREFIX, category, day);
    }

    private String generateWeeklyKey(String category, LocalDate day) {
        WeekFields weekFields = WeekFields.ISO;
        int year = day.get(weekFields.weekBasedYear());
        int weekNumber = day.get(weekFields.weekOfWeekBasedYear());
        return String.format("%s:%s:%d-W%d", LEADERBOARD_KEY_PREFIX, category, year, weekNumber);
    }

    private String generateMonthlyKey(String category, LocalDate day) {
        int year = day.getYear();
        int month = day.getMonthValue();
        return String.format("%s:%s:%d-M%d", LEADERBOARD_KEY_PREFIX, category, year, month);
    }

    private List<LeaderboardResponseDto> getPageByKey(String key, int page, int size) {
        Set<ZSetOperations.TypedTuple<String>> tuples = leaderboardRepository.findPageWithSize(key, page, size);

        Map<String, String> userNicknames = userInfoProvider.loadNicknamesByUserIds(
                tuples.stream()
                        .map(ZSetOperations.TypedTuple::getValue)
                        .toList()
        );

        List<LeaderboardResponseDto> response = new ArrayList<>();
        long rank = (long) (page - 1) * size + 1;
        for (ZSetOperations.TypedTuple<String> tuple : tuples) {
            String userId = tuple.getValue();
            double score = Optional.ofNullable(tuple.getScore()).orElse(0.0);
            response.add(
                    LeaderboardResponseDto.builder()
                            .userId(userId)
                            .nickname(userNicknames.getOrDefault(userId, "Unknown"))
                            .score(score)
                            .rank(rank++)
                            .build()
            );
        }
        return response;
    }
}

