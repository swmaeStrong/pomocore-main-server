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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class GoalServiceImpl implements GoalService {

    private final RedisRepository redisRepository;
    private final CategoryProvider categoryProvider;
    private final LeaderboardProvider leaderboardProvider;

    public static final String GOAL_PREFIX = "goal";

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
    public void saveUserGoal(String userId, SaveUserGoalDto saveUserGoalDto) {
        int goalValue = saveUserGoalDto.goalValue() == null? saveUserGoalDto.goalSeconds() : saveUserGoalDto.goalValue();
        redisRepository.setData(generateKey(userId, saveUserGoalDto.category(), saveUserGoalDto.period()), goalValue);
    }

    @Override
    public List<GoalResponseDto> getCurrentGoals(String userId) {
        List<GoalResponseDto> goalResponseDtoList = new ArrayList<>();
        LocalDate now = LocalDate.now();
        List<String> categoryList = categoryProvider.getCategories();

        for (String category: categoryList) {
            for (PeriodType periodType: PeriodType.values()) {
                String key = generateKey(userId, category, periodType.toString());
                String value = redisRepository.getData(key);
                if (value != null) {
                    goalResponseDtoList.add(GoalResponseDto.builder()
                            .category(category)
                            .currentSeconds((int) leaderboardProvider.getUserScore(userId, category, now, periodType))
                            .goalSeconds(Integer.parseInt(value))
                            .goalValue(Integer.parseInt(value))
                            .periodType(periodType)
                            .build());
                }
            }
        }
        return goalResponseDtoList;
    }

    @Override
    public void deleteUserGoal(String userId, DeleteUserGoalDto deleteUserGoalDto) {
        redisRepository.deleteData(generateKey(userId, deleteUserGoalDto.category(), deleteUserGoalDto.period()));
    }

    private String generateKey(String userId, String category, String period) {
        return String.format("%s:%s:%s:%s", GOAL_PREFIX, userId, category, period.toUpperCase());
    }
}
