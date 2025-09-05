package com.swmStrong.demo.domain.streak.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.streak.dto.DailyActivityResponseDto;
import com.swmStrong.demo.domain.streak.dto.StreakDashboardDto;
import com.swmStrong.demo.domain.streak.dto.StreakResponseDto;
import com.swmStrong.demo.domain.streak.entity.DailyActivity;
import com.swmStrong.demo.domain.streak.entity.Streak;
import com.swmStrong.demo.domain.streak.repository.DailyActivityRepository;
import com.swmStrong.demo.domain.streak.repository.StreakRepository;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StreakServiceImpl implements StreakService {

    private final StreakRepository streakRepository;
    private final DailyActivityRepository dailyActivityRepository;
    private final UserInfoProvider userInfoProvider;

    public StreakServiceImpl(
            StreakRepository streakRepository,
            DailyActivityRepository dailyActivityRepository,
            UserInfoProvider userInfoProvider
    ) {
        this.streakRepository = streakRepository;
        this.dailyActivityRepository = dailyActivityRepository;
        this.userInfoProvider = userInfoProvider;
    }

    @Override
    public StreakResponseDto getStreakCount(String userId) {
        User user = userInfoProvider.loadByUserId(userId);
        if (user == null) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        Streak streak = streakRepository.findByUserId(userId)
                .orElseGet(() ->
                        streakRepository.save(
                                Streak.builder()
                                        .user(user)
                                        .build()
                        )
                );
        return StreakResponseDto.of(streak);
    }

    @Override
    public List<DailyActivityResponseDto> getDailyActivitiesByMonth(String userId, LocalDate date) {
        List<DailyActivity> dailyActivityList = dailyActivityRepository.findByUserIdAndActivityDateBetween(
                userId, date.withDayOfMonth(1), date.withDayOfMonth(date.lengthOfMonth())
        );

        return dailyActivityList.stream()
                .map(DailyActivityResponseDto::of)
                .toList();
    }

    @Override
    public StreakDashboardDto getDailyActivitiesBetweenDateAndDaysBefore(String userId, LocalDate date, Long daysBefore) {
        List<DailyActivity> dailyActivityList = dailyActivityRepository.findByUserIdAndActivityDateBetween(
                userId, date.minusDays(daysBefore), date);

        List<DailyActivityResponseDto> dailyActivityResponseDto = dailyActivityList.stream()
                .map(DailyActivityResponseDto::of)
                .toList();

        StreakResponseDto streak = getStreakCount(userId);

        return StreakDashboardDto.from(streak, dailyActivityResponseDto);

    }
}
