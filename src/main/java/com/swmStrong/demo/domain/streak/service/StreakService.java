package com.swmStrong.demo.domain.streak.service;

import com.swmStrong.demo.domain.streak.dto.DailyActivityResponseDto;
import com.swmStrong.demo.domain.streak.dto.StreakDashboardDto;
import com.swmStrong.demo.domain.streak.dto.StreakResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface StreakService {
    StreakResponseDto getStreakCount(String userId);
    List<DailyActivityResponseDto> getDailyActivitiesByMonth(String userId, LocalDate date);
    StreakDashboardDto getDailyActivitiesBetweenDateAndDaysBefore(String userId, LocalDate date, Long daysBefore);
    List<DailyActivityResponseDto> getWeeklySessionCount(String userId, LocalDate date);
}
