package com.swmStrong.demo.domain.leaderboard.service;

import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategory;

import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.common.util.TimeZoneUtil;
import com.swmStrong.demo.domain.leaderboard.dto.CategoryDetailDto;
import com.swmStrong.demo.domain.leaderboard.dto.LeaderboardResponseDto;
import com.swmStrong.demo.domain.leaderboard.repository.LeaderboardCache;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.message.dto.LeaderBoardUsageMessage;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;
import java.util.*;

@Slf4j
@Service
public class LeaderboardServiceImpl implements LeaderboardService {

    private final LeaderboardCache leaderboardCache;
    private final UserInfoProvider userInfoProvider;
    private final CategoryProvider categoryProvider;
    private final Set<String> workCategories = WorkCategory.categories;

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
    public void increaseSessionCount(String userId, LocalDate date) {
        String category = "sessionCount";
        increaseScoreByCategoryAndUserId(category, userId, date, 1);
    }



    @Override
    public void increaseScoreBatch(List<LeaderBoardUsageMessage> messages) {
        Map<String, Map<String, Double>> categoryUserScoreMap = new HashMap<>();
        Map<String, Double> workUserScoreMap = new HashMap<>();
        
        for (LeaderBoardUsageMessage message : messages) {
            LocalDate day = LocalDateTime.ofInstant(Instant.ofEpochSecond((long) message.timestamp()),
                    ZoneId.systemDefault()).toLocalDate();
            ObjectId categoryObjectId = new ObjectId(message.categoryId());
            String category = categoryProvider.getCategoryById(categoryObjectId);
            
            String dailyKey = generateDailyKey(category, day);
            String weeklyKey = generateWeeklyKey(category, day);
            String monthlyKey = generateMonthlyKey(category, day);
            
            categoryUserScoreMap.computeIfAbsent(dailyKey, k -> new HashMap<>())
                    .merge(message.userId(), message.duration(), Double::sum);
            categoryUserScoreMap.computeIfAbsent(weeklyKey, k -> new HashMap<>())
                    .merge(message.userId(), message.duration(), Double::sum);
            categoryUserScoreMap.computeIfAbsent(monthlyKey, k -> new HashMap<>())
                    .merge(message.userId(), message.duration(), Double::sum);
            
            if (workCategories.contains(category)) {
                String workDailyKey = generateDailyKey("work", day);
                String workWeeklyKey = generateWeeklyKey("work", day);
                String workMonthlyKey = generateMonthlyKey("work", day);
                
                categoryUserScoreMap.computeIfAbsent(workDailyKey, k -> new HashMap<>())
                        .merge(message.userId(), message.duration(), Double::sum);
                categoryUserScoreMap.computeIfAbsent(workWeeklyKey, k -> new HashMap<>())
                        .merge(message.userId(), message.duration(), Double::sum);
                categoryUserScoreMap.computeIfAbsent(workMonthlyKey, k -> new HashMap<>())
                        .merge(message.userId(), message.duration(), Double::sum);
            }
        }
        
        for (Map.Entry<String, Map<String, Double>> keyEntry : categoryUserScoreMap.entrySet()) {
            String key = keyEntry.getKey();
            Map<String, Double> userScoreMap = keyEntry.getValue();
            log.info("key: {}, userScoreMap: {}", key, userScoreMap);
            for (Map.Entry<String, Double> userEntry : userScoreMap.entrySet()) {
                leaderboardCache.increaseScoreByUserId(key, userEntry.getKey(), userEntry.getValue());
            }
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
        return getPageByKey(key, page, size, category, date, periodType);
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
        List<CategoryDetailDto> details = new ArrayList<>();
        if (category.equals("work")) {
            List<String> allCategories = workCategories.stream().toList();

            for (String cat: allCategories) {
                String workCategoryKey = generateKey(cat, date, periodType);
                double categoryScore = leaderboardCache.findScoreByUserId(workCategoryKey, userId);
                details.add(CategoryDetailDto.builder()
                        .category(cat)
                        .score(categoryScore)
                        .build()
                );
            }
        }

        return LeaderboardResponseDto.builder()
                .userId(userId)
                .nickname(userInfoProvider.loadNicknameByUserId(userId))
                .score(leaderboardCache.findScoreByUserId(key, userId))
                .rank(leaderboardCache.findRankByUserId(key, userId) + 1)
                .details(details)
                .build();
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

    public String generateKey(String category, LocalDate date, PeriodType periodType) {
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

    private List<LeaderboardResponseDto> getPageByKey(String key, int page, int size, String category, LocalDate date, PeriodType periodType) {
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

        if (category != null && category.equals("work") && date != null) {
            List<String> allCategories = workCategories.stream().toList();
            
            for (int i = 0; i < response.size(); i++) {
                LeaderboardResponseDto dto = response.get(i);
                List<CategoryDetailDto> details = new ArrayList<>();
                
                for (String cat : allCategories) {
                    String categoryKey = generateKey(cat, date, periodType);
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

    private void increaseScoreByCategoryAndUserId(String category, String userId, LocalDate day, double duration) {
        String dailyKey = generateDailyKey(category, day);
        leaderboardCache.increaseScoreByUserId(dailyKey, userId, duration);
        String weeklyKey = generateWeeklyKey(category, day);
        leaderboardCache.increaseScoreByUserId(weeklyKey, userId, duration);
        String monthlyKey = generateMonthlyKey(category, day);
        leaderboardCache.increaseScoreByUserId(monthlyKey, userId, duration);
    }
}

