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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("StreakServiceImpl 테스트")
class StreakServiceImplTest {

    @Mock
    private StreakRepository streakRepository;

    @Mock
    private DailyActivityRepository dailyActivityRepository;

    @Mock
    private UserInfoProvider userInfoProvider;

    @InjectMocks
    private StreakServiceImpl streakService;

    private User testUser;
    private Streak testStreak;
    private String userId;

    @BeforeEach
    void setUp() {
        testUser = new User(userId, "테스트사용자");

        testStreak = Streak.builder()
                .user(testUser)
                .build();
        testStreak.plusStreak();
        testStreak.plusStreak();
        testStreak.renewLastActiveDate(LocalDate.now());
    }

    @Test
    @DisplayName("스트릭 카운트 조회 - 기존 스트릭이 있는 경우")
    void shouldGetExistingStreakCount() {
        // given
        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(testStreak));

        // when
        StreakResponseDto result = streakService.getStreakCount(userId);

        // then
        assertThat(result.currentStreak()).isEqualTo(2);
        assertThat(result.maxStreak()).isEqualTo(2);
        verify(streakRepository, never()).save(any());
    }

    @Test
    @DisplayName("스트릭 카운트 조회 - 신규 스트릭 생성")
    void shouldCreateNewStreakCount() {
        // given
        Streak newStreak = Streak.builder()
                .user(testUser)
                .build();

        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(streakRepository.save(any(Streak.class))).thenReturn(newStreak);

        // when
        StreakResponseDto result = streakService.getStreakCount(userId);

        // then
        assertThat(result.currentStreak()).isEqualTo(0);
        assertThat(result.maxStreak()).isEqualTo(0);
        verify(streakRepository).save(any(Streak.class));
    }

    @Test
    @DisplayName("스트릭 카운트 조회 실패 - 사용자가 없는 경우")
    void shouldFailGetStreakCountWithNullUser() {
        // given
        when(userInfoProvider.loadByUserId(userId)).thenReturn(null);

        // when & then
        assertThatThrownBy(() -> streakService.getStreakCount(userId))
                .isInstanceOf(ApiException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.USER_NOT_FOUND);
    }

    @Test
    @DisplayName("월별 일일 활동 조회")
    void shouldGetDailyActivitiesByMonth() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        LocalDate startOfMonth = date.withDayOfMonth(1);
        LocalDate endOfMonth = date.withDayOfMonth(31);

        DailyActivity activity1 = DailyActivity.builder()
                .user(testUser)
                .activityDate(LocalDate.of(2024, 1, 5))
                .build();

        DailyActivity activity2 = DailyActivity.builder()
                .user(testUser)
                .activityDate(LocalDate.of(2024, 1, 10))
                .build();

        List<DailyActivity> activities = Arrays.asList(activity1, activity2);

        when(dailyActivityRepository.findByUserIdAndActivityDateBetween(userId, startOfMonth, endOfMonth))
                .thenReturn(activities);

        // when
        List<DailyActivityResponseDto> result = streakService.getDailyActivitiesByMonth(userId, date);

        // then
        assertThat(result).hasSize(2);
    }

    @Test
    @DisplayName("기간별 일일 활동 조회 및 대시보드 정보")
    void shouldGetDailyActivitiesBetweenDateAndDaysBefore() {
        // given
        LocalDate endDate = LocalDate.of(2024, 1, 10);
        Long daysBefore = 7L;
        LocalDate startDate = endDate.minusDays(daysBefore);

        DailyActivity activity1 = DailyActivity.builder()
                .user(testUser)
                .activityDate(LocalDate.of(2024, 1, 5))
                .build();

        DailyActivity activity2 = DailyActivity.builder()
                .user(testUser)
                .activityDate(LocalDate.of(2024, 1, 8))
                .build();

        List<DailyActivity> activities = Arrays.asList(activity1, activity2);

        when(dailyActivityRepository.findByUserIdAndActivityDateBetween(userId, startDate, endDate))
                .thenReturn(activities);
        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);
        when(streakRepository.findByUserId(userId)).thenReturn(Optional.of(testStreak));

        // when
        StreakDashboardDto result = streakService.getDailyActivitiesBetweenDateAndDaysBefore(userId, endDate, daysBefore);

        // then
        assertThat(result.dailyActivities()).hasSize(2);
    }

    @Test
    @DisplayName("주간 세션 카운트 조회")
    void shouldGetWeeklySessionCount() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 10); // 수요일
        LocalDate startOfWeek = LocalDate.of(2024, 1, 8); // 월요일

        DailyActivity activity1 = DailyActivity.builder()
                .user(testUser)
                .activityDate(LocalDate.of(2024, 1, 8))
                .build();

        DailyActivity activity2 = DailyActivity.builder()
                .user(testUser)
                .activityDate(LocalDate.of(2024, 1, 9))
                .build();

        DailyActivity activity3 = DailyActivity.builder()
                .user(testUser)
                .activityDate(LocalDate.of(2024, 1, 10))
                .build();

        List<DailyActivity> activities = Arrays.asList(activity1, activity2, activity3);

        when(dailyActivityRepository.findByUserIdAndActivityDateBetween(eq(userId), eq(startOfWeek), eq(date)))
                .thenReturn(activities);

        // when
        List<DailyActivityResponseDto> result = streakService.getWeeklySessionCount(userId, date);

        // then
        assertThat(result).hasSize(3);
    }

    @Test
    @DisplayName("빈 일일 활동 목록 조회")
    void shouldReturnEmptyDailyActivities() {
        // given
        LocalDate date = LocalDate.of(2024, 1, 15);
        when(dailyActivityRepository.findByUserIdAndActivityDateBetween(any(), any(), any()))
                .thenReturn(List.of());

        // when
        List<DailyActivityResponseDto> result = streakService.getDailyActivitiesByMonth(userId, date);

        // then
        assertThat(result).isEmpty();
    }
}