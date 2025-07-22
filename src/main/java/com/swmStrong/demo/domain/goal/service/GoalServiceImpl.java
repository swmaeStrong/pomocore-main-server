package com.swmStrong.demo.domain.goal.service;

import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.goal.dto.DeleteUserGoalDto;
import com.swmStrong.demo.domain.goal.dto.GoalResponseDto;
import com.swmStrong.demo.domain.goal.dto.SaveUserGoalDto;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;

@Service
public class GoalServiceImpl implements GoalService {

    private final RedisRepository redisRepository;
    private final CategoryProvider categoryProvider;
    private final LeaderboardProvider leaderboardProvider;

    private static final String GOAL_PREFIX = "goal";

    public GoalServiceImpl(
            RedisRepository redisRepository,
            CategoryProvider categoryProvider,
            LeaderboardProvider leaderboardProvider
            ) {
        this.redisRepository = redisRepository;
        this.categoryProvider = categoryProvider;
        this.leaderboardProvider = leaderboardProvider;
    }

    @Override
    public void saveUserGoal(String userId, List<SaveUserGoalDto> saveUserGoalDtoList) {
        for (SaveUserGoalDto saveUserGoalDto : saveUserGoalDtoList) {
            redisRepository.setData(generateKey(userId, saveUserGoalDto.category(), saveUserGoalDto.period(), LocalDate.now()), saveUserGoalDto.goalSeconds());
        }
    }

    @Override
    public List<GoalResponseDto> getUserGoals(String userId, LocalDate date) {
        List<GoalResponseDto> goalResponseDtoList = new ArrayList<>();

        for (String category: getCategoryList()) {
            for (PeriodType periodType: PeriodType.values()) {
                String key = generateKey(userId, category, periodType.toString(), LocalDate.now());
                if (redisRepository.getData(key) != null) {
                    goalResponseDtoList.add(GoalResponseDto.builder()
                            .category(category)
                            .currentSeconds((int) leaderboardProvider.getUserScore(userId, category, date, periodType))
                            .goalSeconds(Integer.parseInt(redisRepository.getData(key)))
                            .periodType(periodType)
                            .build());
                }
            }
        }
        return goalResponseDtoList;
    }

    @Override
    public void deleteUserGoal(String userId, DeleteUserGoalDto deleteUserGoalDto) {
        redisRepository.deleteData(generateKey(userId, deleteUserGoalDto.category(), deleteUserGoalDto.period(), LocalDate.now()));
    }

    private String generateKey(String userId, String category, String period, LocalDate day) {
        PeriodType periodType = PeriodType.valueOf(period.toUpperCase());

        return switch (periodType) {
            case DAILY ->  generateDailyKey(userId, category, day);
            case WEEKLY -> generateWeeklyKey(userId, category, day);
            case MONTHLY -> generateMonthlyKey(userId, category, day);
        };
    }

    private String generateDailyKey(String userId, String category, LocalDate day) {
        return String.format("%s:%s:%s:%s", GOAL_PREFIX, userId, category, day);
    }

    private String generateWeeklyKey(String userId, String category, LocalDate day) {
        WeekFields weekFields = WeekFields.ISO;
        int year = day.get(weekFields.weekBasedYear());
        int weekNumber = day.get(weekFields.weekOfWeekBasedYear());
        return String.format("%s:%s:%s:%d-%d", GOAL_PREFIX, userId, category, year, weekNumber);
    }

    private String generateMonthlyKey(String userId, String category, LocalDate day) {
        int year = day.getYear();
        int month = day.getMonthValue();
        return String.format("%s:%s:%s:%d-M%d", GOAL_PREFIX, userId, category, year, month);
    }
    private List<String> getCategoryList() {
        return categoryProvider.getCategories();
    }
}
