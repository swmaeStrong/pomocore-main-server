package com.swmStrong.demo.domain.goal.scheduler;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.goal.entity.GoalResult;
import com.swmStrong.demo.domain.goal.repository.GoalResultRepository;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.swmStrong.demo.domain.goal.service.GoalServiceImpl.GOAL_PREFIX;

@Service
@Slf4j
public class GoalResultScheduler {

    private final GoalResultRepository goalResultRepository;
    private final RedisRepository redisRepository;
    private final LeaderboardProvider leaderboardProvider;
    private final UserInfoProvider userInfoProvider;

    public GoalResultScheduler(
            GoalResultRepository goalResultRepository,
            RedisRepository redisRepository,
            LeaderboardProvider leaderboardProvider,
            UserInfoProvider userInfoProvider
    ) {
        this.goalResultRepository = goalResultRepository;
        this.redisRepository = redisRepository;
        this.leaderboardProvider = leaderboardProvider;
        this.userInfoProvider = userInfoProvider;
    }

    @Async
    @Scheduled(cron = "0 0 0 * * *")
    public void saveUserGoalResultDaily() {
        saveUserGoalResult("DAILY");
    }

    @Async
    @Scheduled(cron = "0 0 0 * * mon")
    public void saveUserGoalResultWeekly() {
        saveUserGoalResult("WEEKLY");
    }

    @Async
    @Scheduled(cron = "0 0 0 1 * *")
    public void saveUserGoalResultMonthly() {
        saveUserGoalResult("MONTHLY");
    }


    public void saveUserGoalResult(String period) {
        LocalDate date = LocalDate.now().minusDays(1);
        Set<String> goalKeys = redisRepository.findKeys(GOAL_PREFIX + ":*" + period);
        List<GoalResult> goalResultList = new ArrayList<>();
        for (String key : goalKeys) {
            ParsedKey parsedKey = parseKey(key);
            int goal = Integer.parseInt(redisRepository.getData(key));
            int achieved = (int) leaderboardProvider.getUserScore(parsedKey.userId(), parsedKey.category(), date, parsedKey.periodType());
            //TODO: user 없으면 넘기기 (탈퇴 등)
            goalResultList.add(GoalResult.builder()
                    .user(userInfoProvider.loadByUserId(parsedKey.userId()))
                    .goalSeconds(goal)
                    .achievedSeconds(achieved)
                    .date(date)
                    .periodType(parsedKey.periodType())
                    .build()
            );
        }
        goalResultRepository.saveAll(goalResultList);
    }

    public ParsedKey parseKey(String key) {
        String[] values = key.split(":");

        if (values.length != 4) {
            throw new ApiException(ErrorCode.INVALID_KEY);
        }

        String userId = values[1];
        String category = values[2];
        PeriodType periodType = PeriodType.valueOf(values[3]);

        return ParsedKey.builder()
                .userId(userId)
                .category(category)
                .periodType(periodType)
                .build();
    }

    @Builder
    public record ParsedKey(
            String userId,
            String category,
            PeriodType periodType
    ) {}
}
