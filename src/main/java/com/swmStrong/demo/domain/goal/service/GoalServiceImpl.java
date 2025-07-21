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

@Service
public class GoalServiceImpl implements GoalService {

    private final RedisRepository redisRepository;
    private final CategoryProvider categoryProvider;
    private final LeaderboardProvider leaderboardProvider;

    private static final String GOAL_PREFIX = "goal:";

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
        redisRepository.setData(generateKey(userId, saveUserGoalDto.category()), saveUserGoalDto.goalSeconds());
    }

    @Override
    public List<GoalResponseDto> getUserGoals(String userId, LocalDate date, String period) {
        List<GoalResponseDto> goalResponseDtoList = new ArrayList<>();
        PeriodType periodType = PeriodType.valueOf(period.toUpperCase());

        for (String category: getCategoryList()) {
            String key = generateKey(userId, category);
            if (redisRepository.getData(key) != null) {
                goalResponseDtoList.add(new GoalResponseDto(category, (int) leaderboardProvider.getUserScore(userId, category, date, periodType), Integer.parseInt(redisRepository.getData(key))));
            }
        }
        return goalResponseDtoList;
    }

    @Override
    public void deleteUserGoal(String userId, DeleteUserGoalDto deleteUserGoalDto) {
        redisRepository.deleteData(generateKey(userId, deleteUserGoalDto.category()));
    }

    private String generateKey(String userId, String category) {
        return GOAL_PREFIX + userId + ":" + category;
    }

    private List<String> getCategoryList() {
        return categoryProvider.getCategories();
    }
}
