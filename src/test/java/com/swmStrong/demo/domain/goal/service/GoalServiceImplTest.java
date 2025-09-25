package com.swmStrong.demo.domain.goal.service;

import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.goal.dto.DeleteUserGoalDto;
import com.swmStrong.demo.domain.goal.dto.GoalResponseDto;
import com.swmStrong.demo.domain.goal.dto.SaveUserGoalDto;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GoalServiceImpl 테스트")
class GoalServiceImplTest {

    @Mock
    private RedisRepository redisRepository;

    @Mock
    private CategoryProvider categoryProvider;

    @Mock
    private LeaderboardProvider leaderboardProvider;

    @InjectMocks
    private GoalServiceImpl goalService;

    private String userId;
    private String category;

    @BeforeEach
    void setUp() {
        userId = "user123";
        category = "개발";
    }

    @Test
    @DisplayName("사용자 목표 저장 - goalValue 사용")
    void shouldSaveUserGoalWithGoalValue() {
        // given
        SaveUserGoalDto saveDto = new SaveUserGoalDto(category, 7200, null, "DAILY");
        String expectedKey = "goal:user123:개발:DAILY";

        // when
        goalService.saveUserGoal(userId, saveDto);

        // then
        verify(redisRepository).setData(expectedKey, 7200);
    }

    @Test
    @DisplayName("사용자 목표 저장 - goalSeconds 사용")
    void shouldSaveUserGoalWithGoalSeconds() {
        // given
        SaveUserGoalDto saveDto = new SaveUserGoalDto(category, null, 3600, "WEEKLY");
        String expectedKey = "goal:user123:개발:WEEKLY";

        // when
        goalService.saveUserGoal(userId, saveDto);

        // then
        verify(redisRepository).setData(expectedKey, 3600);
    }

    @Test
    @DisplayName("사용자 목표 저장 - goalValue 우선 사용")
    void shouldSaveUserGoalPreferGoalValue() {
        // given
        SaveUserGoalDto saveDto = new SaveUserGoalDto(category, 7200, 3600, "MONTHLY");
        String expectedKey = "goal:user123:개발:MONTHLY";

        // when
        goalService.saveUserGoal(userId, saveDto);

        // then
        verify(redisRepository).setData(expectedKey, 7200);
    }

    @Test
    @DisplayName("현재 목표 조회 - 단일 목표")
    void shouldGetCurrentGoalsSingle() {
        // given
        List<String> categories = Arrays.asList("개발");
        String goalKey = "goal:user123:개발:DAILY";

        when(categoryProvider.getCategories()).thenReturn(categories);
        when(redisRepository.getData(goalKey)).thenReturn("7200");
        when(redisRepository.getData(anyString())).thenReturn(null);
        when(redisRepository.getData(eq(goalKey))).thenReturn("7200");
        when(leaderboardProvider.getUserScore(eq(userId), eq("개발"), any(LocalDate.class), eq(PeriodType.DAILY)))
                .thenReturn(3600.0);

        // when
        List<GoalResponseDto> result = goalService.getCurrentGoals(userId);

        // then
        assertThat(result).hasSize(1);
        GoalResponseDto goalDto = result.get(0);
        assertThat(goalDto.category()).isEqualTo("개발");
        assertThat(goalDto.currentSeconds()).isEqualTo(3600);
        assertThat(goalDto.goalSeconds()).isEqualTo(7200);
        assertThat(goalDto.goalValue()).isEqualTo(7200);
        assertThat(goalDto.periodType()).isEqualTo(PeriodType.DAILY);
    }

    @Test
    @DisplayName("현재 목표 조회 - 다중 카테고리 및 기간")
    void shouldGetCurrentGoalsMultiple() {
        // given
        List<String> categories = Arrays.asList("개발", "학습");

        when(categoryProvider.getCategories()).thenReturn(categories);

        // 개발 - DAILY 목표
        when(redisRepository.getData("goal:user123:개발:DAILY")).thenReturn("7200");
        when(leaderboardProvider.getUserScore(userId, "개발", any(LocalDate.class), PeriodType.DAILY))
                .thenReturn(3600.0);

        // 학습 - WEEKLY 목표
        when(redisRepository.getData("goal:user123:학습:WEEKLY")).thenReturn("14400");
        when(leaderboardProvider.getUserScore(userId, "학습", any(LocalDate.class), PeriodType.WEEKLY))
                .thenReturn(7200.0);

        // 나머지는 null
        when(redisRepository.getData("goal:user123:개발:WEEKLY")).thenReturn(null);
        when(redisRepository.getData("goal:user123:개발:MONTHLY")).thenReturn(null);
        when(redisRepository.getData("goal:user123:학습:DAILY")).thenReturn(null);
        when(redisRepository.getData("goal:user123:학습:MONTHLY")).thenReturn(null);

        // when
        List<GoalResponseDto> result = goalService.getCurrentGoals(userId);

        // then
        assertThat(result).hasSize(2);

        GoalResponseDto dailyGoal = result.stream()
                .filter(goal -> goal.periodType() == PeriodType.DAILY)
                .findFirst()
                .orElse(null);
        assertThat(dailyGoal).isNotNull();
        assertThat(dailyGoal.category()).isEqualTo("개발");
        assertThat(dailyGoal.currentSeconds()).isEqualTo(3600);
        assertThat(dailyGoal.goalSeconds()).isEqualTo(7200);

        GoalResponseDto weeklyGoal = result.stream()
                .filter(goal -> goal.periodType() == PeriodType.WEEKLY)
                .findFirst()
                .orElse(null);
        assertThat(weeklyGoal).isNotNull();
        assertThat(weeklyGoal.category()).isEqualTo("학습");
        assertThat(weeklyGoal.currentSeconds()).isEqualTo(7200);
        assertThat(weeklyGoal.goalSeconds()).isEqualTo(14400);
    }

    @Test
    @DisplayName("현재 목표 조회 - 목표가 없는 경우")
    void shouldGetCurrentGoalsEmpty() {
        // given
        List<String> categories = Arrays.asList("개발");
        when(categoryProvider.getCategories()).thenReturn(categories);
        when(redisRepository.getData(anyString())).thenReturn(null);

        // when
        List<GoalResponseDto> result = goalService.getCurrentGoals(userId);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("사용자 목표 삭제")
    void shouldDeleteUserGoal() {
        // given
        DeleteUserGoalDto deleteDto = new DeleteUserGoalDto(category, "DAILY");
        String expectedKey = "goal:user123:개발:DAILY";

        // when
        goalService.deleteUserGoal(userId, deleteDto);

        // then
        verify(redisRepository).deleteData(expectedKey);
    }

    @Test
    @DisplayName("키 생성 로직 검증 - 다양한 기간")
    void shouldGenerateCorrectKeys() {
        // given
        SaveUserGoalDto dailyDto = new SaveUserGoalDto(category, 3600, null, "daily");
        SaveUserGoalDto weeklyDto = new SaveUserGoalDto(category, 7200, null, "weekly");
        SaveUserGoalDto monthlyDto = new SaveUserGoalDto(category, 14400, null, "monthly");

        // when
        goalService.saveUserGoal(userId, dailyDto);
        goalService.saveUserGoal(userId, weeklyDto);
        goalService.saveUserGoal(userId, monthlyDto);

        // then
        verify(redisRepository).setData("goal:user123:개발:DAILY", 3600);
        verify(redisRepository).setData("goal:user123:개발:WEEKLY", 7200);
        verify(redisRepository).setData("goal:user123:개발:MONTHLY", 14400);
    }

    @Test
    @DisplayName("키 생성 로직 검증 - 대소문자 처리")
    void shouldGenerateKeysWithUpperCase() {
        // given
        DeleteUserGoalDto deleteDto = new DeleteUserGoalDto(category, "weekly");

        // when
        goalService.deleteUserGoal(userId, deleteDto);

        // then
        verify(redisRepository).deleteData("goal:user123:개발:WEEKLY");
    }

    @Test
    @DisplayName("목표 조회 시 모든 PeriodType 확인")
    void shouldCheckAllPeriodTypes() {
        // given
        List<String> categories = Arrays.asList("개발");
        when(categoryProvider.getCategories()).thenReturn(categories);
        when(redisRepository.getData(anyString())).thenReturn(null);

        // when
        goalService.getCurrentGoals(userId);

        // then
        verify(redisRepository).getData("goal:user123:개발:DAILY");
        verify(redisRepository).getData("goal:user123:개발:WEEKLY");
        verify(redisRepository).getData("goal:user123:개발:MONTHLY");
    }

    @Test
    @DisplayName("목표 조회 시 리더보드 점수 호출 확인")
    void shouldCallLeaderboardForCurrentScore() {
        // given
        List<String> categories = Arrays.asList("개발");
        LocalDate today = LocalDate.now();

        when(categoryProvider.getCategories()).thenReturn(categories);
        when(redisRepository.getData("goal:user123:개발:DAILY")).thenReturn("7200");
        when(redisRepository.getData("goal:user123:개발:WEEKLY")).thenReturn(null);
        when(redisRepository.getData("goal:user123:개발:MONTHLY")).thenReturn(null);
        when(leaderboardProvider.getUserScore(userId, "개발", today, PeriodType.DAILY))
                .thenReturn(3600.0);

        // when
        goalService.getCurrentGoals(userId);

        // then
        verify(leaderboardProvider).getUserScore(userId, "개발", today, PeriodType.DAILY);
    }
}