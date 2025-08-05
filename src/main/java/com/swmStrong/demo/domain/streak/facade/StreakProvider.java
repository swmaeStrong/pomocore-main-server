package com.swmStrong.demo.domain.streak.facade;

import com.swmStrong.demo.domain.streak.entity.Streak;
import com.swmStrong.demo.domain.streak.repository.DailyActivityRepository;
import com.swmStrong.demo.domain.streak.repository.StreakRepository;
import org.springframework.stereotype.Service;

@Service
public class StreakProvider {

    private final StreakRepository streakRepository;
    private final DailyActivityRepository dailyActivityRepository;

    public StreakProvider(
            StreakRepository streakRepository,
            DailyActivityRepository dailyActivityRepository
    ) {
        this.streakRepository = streakRepository;
        this.dailyActivityRepository = dailyActivityRepository;
    }

    public Streak loadStreakByUserId(String userId) {
        return streakRepository.findByUserId(userId).orElse(null);
    }

    public Integer loadTotalSessionByUserId(String userId) {
        return dailyActivityRepository.sumActivityCountByUserId(userId);
    }
}
