package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategoryType;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.common.util.TimeZoneUtil;
import com.swmStrong.demo.domain.leaderboard.dto.CategoryDetailDto;
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
    private final Set<String> workCategories = WorkCategoryType.getAllValues();

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
        log.trace("Increase score for user: {} category: {}, duration: {}", userId, category, duration);

        increaseScoreByCategoryAndUserId(category, userId, day, duration);
        if (workCategories.contains(category)) {
            increaseScoreByCategoryAndUserId("total", userId, day, duration);
        }
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
        return getPageByKey(key, page, size, category, date);
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
        return getPageByKey(key, page, size, null, null);
    }

    private List<LeaderboardResponseDto> getPageByKey(String key, int page, int size, String category, LocalDate date) {
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
                            .details(null)
                            .build()
            );
        }

        if (category != null && category.equals("total") && date != null) {
            List<String> allCategories = workCategories.stream().toList();
            
            for (int i = 0; i < response.size(); i++) {
                LeaderboardResponseDto dto = response.get(i);
                List<CategoryDetailDto> details = new ArrayList<>();
                
                for (String cat : allCategories) {
                    String categoryKey = generateDailyKey(cat, date);
                    double categoryScore = leaderboardCache.findScoreByUserId(categoryKey, dto.userId());
                    
                    details.add(CategoryDetailDto.builder()
                            .category(cat)
                            .score(categoryScore)
                            .build());
                }
                
                response.set(i, LeaderboardResponseDto.builder()
                        .userId(dto.userId())
                        .nickname(dto.nickname())
                        .score(dto.score())
                        .rank(dto.rank())
                        .details(details)
                        .build());
            }
        }

        return response;
    }

    private LeaderboardResponseDto getUserInfo(String userId, String key) {

        return LeaderboardResponseDto.builder()
                .userId(userId)
                .nickname(userInfoProvider.loadNicknameByUserId(userId))
                .score(leaderboardCache.findScoreByUserId(key, userId))
                .rank(leaderboardCache.findRankByUserId(key, userId) + 1)
                .details(null)
                .build();
    }

    private void increaseScoreByCategoryAndUserId(String category, String userId, LocalDate day, double duration) {
        String dailyKey = generateDailyKey(category, day);
        leaderboardCache.increaseScoreByUserId(dailyKey, userId, duration);
        String weeklyKey = generateWeeklyKey(category, day);
        leaderboardCache.increaseScoreByUserId(weeklyKey, userId, duration);
        String monthlyKey = generateMonthlyKey(category, day);
        leaderboardCache.increaseScoreByUserId(monthlyKey, userId, duration);
    }
}

