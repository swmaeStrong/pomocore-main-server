package com.swmStrong.demo.domain.streak.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.streak.dto.DailyActivityResponseDto;
import com.swmStrong.demo.domain.streak.dto.StreakResponseDto;
import com.swmStrong.demo.domain.streak.entity.DailyActivity;
import com.swmStrong.demo.domain.streak.repository.DailyActivityRepository;
import com.swmStrong.demo.domain.streak.repository.StreakRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class StreakServiceImpl implements StreakService {

    private final StreakRepository streakRepository;
    private final DailyActivityRepository dailyActivityRepository;

    public StreakServiceImpl(
            StreakRepository streakRepository,
            DailyActivityRepository dailyActivityRepository
    ) {
        this.streakRepository = streakRepository;
        this.dailyActivityRepository = dailyActivityRepository;
    }

    @Override
    public StreakResponseDto getStreakCount(String userId) {
        return StreakResponseDto.of(streakRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(ErrorCode.USER_NOT_FOUND)));
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
}
