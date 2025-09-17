package com.swmStrong.demo.domain.streak.listener;

import com.swmStrong.demo.domain.streak.entity.DailyActivity;
import com.swmStrong.demo.domain.streak.repository.DailyActivityRepository;
import com.swmStrong.demo.domain.streak.entity.Streak;
import com.swmStrong.demo.domain.streak.repository.StreakRepository;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.message.event.UsageLogCreatedEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class StreakEventListener {

    private final StreakRepository streakRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final UserInfoProvider userInfoProvider;

    public StreakEventListener(
            StreakRepository streakRepository,
            DailyActivityRepository dailyActivityRepository,
            UserInfoProvider userInfoProvider
    ) {
        this.streakRepository = streakRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.userInfoProvider = userInfoProvider;
    }

    @Async
    @Transactional
    @EventListener
    public void handleUsageLogCreated(UsageLogCreatedEvent event) {
        log.trace("event listened, userId={}, date={}",
                event.userId(), event.activityDate()
        );
        handleStreak(event);
        handleDailyActivity(event);
    }

    private void handleDailyActivity(UsageLogCreatedEvent event) {
        DailyActivity activity = dailyActivityRepository.findByUserIdAndActivityDate(event.userId(), event.activityDate())
                .orElse(DailyActivity.builder()
                        .user(userInfoProvider.loadByUserId(event.userId()))
                        .activityDate(event.activityDate())
                        .build()
                );

        activity.increaseActivityCount();
        dailyActivityRepository.save(activity);
    }

    private void handleStreak(UsageLogCreatedEvent event) {
        Streak streak = streakRepository.findByUserId(event.userId())
                .orElse(Streak.builder()
                        .user(userInfoProvider.loadByUserId(event.userId()))
                        .build()
                );

        LocalDate lastActiveDate = streak.getLastActiveDate();
        LocalDate eventDate = event.activityDate();

        if (lastActiveDate != null && !eventDate.isAfter(lastActiveDate)) {
            log.warn("Ignoring event with past or duplicate date. userId={}, eventDate={}, lastActiveDate={}",
                    event.userId(), eventDate, lastActiveDate);
            return;
        }

        if (lastActiveDate != null && eventDate.isAfter(lastActiveDate.plusDays(1))) {
            streak.resetCurrentStreak();
        }

        streak.plusStreak();
        streak.renewLastActiveDate(eventDate);

        streakRepository.save(streak);
    }
}
