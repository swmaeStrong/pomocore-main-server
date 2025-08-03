package com.swmStrong.demo.domain.streak.scheduler;

import com.swmStrong.demo.domain.streak.entity.Streak;
import com.swmStrong.demo.domain.streak.repository.StreakRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StreakScheduler {

    private final StreakRepository streakRepository;

    public  StreakScheduler(StreakRepository streakRepository) {
        this.streakRepository = streakRepository;
    }
    @Scheduled(cron = "0 0 0 * * *")
    public void syncTimeAndStreak() {
        List<Streak> streakList = streakRepository.findAll();

        for (Streak streak: streakList) {
            if (streak.getLastActiveDate() == null) continue;
            if (streak.getLastActiveDate().isBefore(LocalDate.now().minusDays(1))) {
                streak.resetCurrentStreak();
            }
        }

        streakRepository.saveAll(streakList);
    }
}
