package com.swmStrong.demo.domain.goal.scheduler;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.goal.entity.GoalResult;
import com.swmStrong.demo.domain.goal.repository.GoalResultRepository;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.swmStrong.demo.domain.goal.service.GoalServiceImpl.GOAL_PREFIX;

@Service
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
    public void saveUserGoalResult() {
        Set<String> goalKeys = redisRepository.findKeys(GOAL_PREFIX + ":*");
        List<GoalResult> goalResultList = new ArrayList<>();
        for (String key : goalKeys) {
            ParsedKey parsedKey = parseKey(key);
            int goal = Integer.parseInt(redisRepository.getData(key));
            int achieved = (int) leaderboardProvider.getUserScore(parsedKey.userId(), parsedKey.category(), parsedKey.date(), parsedKey.periodType());

            goalResultList.add(GoalResult.builder()
                    .user(userInfoProvider.loadByUserId(parsedKey.userId()))
                    .goalSeconds(goal)
                    .achievedSeconds(achieved)
                    .date(parsedKey.date())
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
        String dateStr = values[3];

        LocalDate date;
        PeriodType periodType;

        if (dateStr.contains("M")) {
            periodType = PeriodType.MONTHLY;
            String[] parts = dateStr.split("-M");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            date = LocalDate.of(year, month, 1);
        } else if (dateStr.contains("-") && dateStr.split("-").length == 2 && !dateStr.matches("\\d{4}-\\d{2}-\\d{2}")) {
            periodType = PeriodType.WEEKLY;
            String[] parts = dateStr.split("-");
            int year = Integer.parseInt(parts[0]);
            int weekNumber = Integer.parseInt(parts[1]);
            WeekFields weekFields = WeekFields.ISO;
            date = LocalDate.of(year, 1, 1)
                    .with(weekFields.weekBasedYear(), year)
                    .with(weekFields.weekOfWeekBasedYear(), weekNumber)
                    .with(weekFields.dayOfWeek(), 1);
        } else {
            periodType = PeriodType.DAILY;
            date = LocalDate.parse(dateStr);
        }

        return new ParsedKey(userId, category, date, periodType);
    }

    public record ParsedKey(
            String userId,
            String category,
            LocalDate date,
            PeriodType periodType
    ) {}
}
