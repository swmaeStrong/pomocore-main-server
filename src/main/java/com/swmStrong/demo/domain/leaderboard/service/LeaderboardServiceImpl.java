package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.common.util.TimeZoneUtil;
import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardCache;
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

    private final LeaderboardCache leaderboardCache;
    private final UserInfoProvider userInfoProvider;
    private final CategoryProvider categoryProvider;

    public static final String LEADERBOARD_KEY_PREFIX = "leaderboard";

    public LeaderboardServiceImpl(
            LeaderboardCache leaderboardCache,
            UserInfoProvider userInfoProvider,
            CategoryProvider categoryProvider
    ) {
        this.leaderboardCache = leaderboardCache;
        this.userInfoProvider = userInfoProvider;
        this.categoryProvider = categoryProvider;
    }

    @Override
    public void increaseScore(String categoryId, String userId, double duration, double timestamp) {
        LocalDate day = LocalDateTime.ofInstant(java.time.Instant.ofEpochSecond((long) timestamp),
                java.time.ZoneId.systemDefault()).toLocalDate();
        ObjectId categoryObjectId = new ObjectId(categoryId);
        String category = categoryProvider.getCategoryById(categoryObjectId);
        log.info("Increase score for user: {} category: {}, duration: {}", userId, category, duration);

        String dailyKey = generateDailyKey(category, day);
        leaderboardCache.increaseScoreByUserId(dailyKey, userId, duration);
        String weeklyKey = generateWeeklyKey(category, day);
        leaderboardCache.increaseScoreByUserId(weeklyKey, userId, duration);
        String monthlyKey = generateMonthlyKey(category, day);
        leaderboardCache.increaseScoreByUserId(monthlyKey, userId, duration);
    }

    @Override
    public List<LeaderboardResponseDto> getLeaderboardPage(
            String category,
            int page,
            int size,
            LocalDate date,
            PeriodType periodType
    ) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateKey(category, date, periodType);
        return getPageByKey(key, page, size);
    }

    @Override
    public LeaderboardResponseDto getUserScoreInfo(
            String category,
            String userId,
            LocalDate date,
            PeriodType periodType
    ) {
        if (date == null) {
            date = LocalDate.now();
        }
        String key = generateKey(category, date, periodType);
        return getUserInfo(userId, key);
    }

    @Override
    public List<LeaderboardResponseDto> getAllLeaderboard(String category, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        String key = generateDailyKey(category, date);
        Set<ZSetOperations.TypedTuple<String>> tuples = leaderboardCache.findAll(key);

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

        LocalDate date = TimeZoneUtil.todayInTimezone(TimeZoneUtil.KOREA_TIMEZONE);
        List<String> categories = categoryProvider.getCategories();

        for (String category : categories) {
            response.put(category, getLeaderboardPage(category, 1, 10, date, PeriodType.DAILY));
        }
        return response;
    }

    private String generateKey(String category, LocalDate date, PeriodType periodType) {
        return switch (periodType) {
            case DAILY -> generateDailyKey(category, date);
            case WEEKLY -> generateWeeklyKey(category, date);
            case MONTHLY -> generateMonthlyKey(category, date);
        };
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
        Set<ZSetOperations.TypedTuple<String>> tuples = leaderboardCache.findPageWithSize(key, page, size);

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

    private LeaderboardResponseDto getUserInfo(String userId, String key) {

        return LeaderboardResponseDto.builder()
                .userId(userId)
                .nickname(userInfoProvider.loadNicknameByUserId(userId))
                .score(leaderboardCache.findScoreByUserId(key, userId))
                .rank(leaderboardCache.findRankByUserId(key, userId) + 1)
                .build();
    }
}

