package com.swmStrong.demo.domain.goal.entity;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import com.swmStrong.demo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GoalResult 엔티티 테스트")
class GoalResultTest {

    @Test
    @DisplayName("Builder로 일일 목표 결과 생성")
    void shouldCreateDailyGoalResultWithBuilder() {
        // given
        User user = new User("user123", "테스트사용자");
        String category = "개발";
        LocalDate date = LocalDate.of(2024, 1, 1);
        PeriodType periodType = PeriodType.DAILY;
        int goalSeconds = 7200; // 2시간
        int achievedSeconds = 5400; // 1시간 30분

        // when
        GoalResult goalResult = GoalResult.builder()
                .user(user)
                .category(category)
                .date(date)
                .periodType(periodType)
                .goalSeconds(goalSeconds)
                .achievedSeconds(achievedSeconds)
                .build();

        // then
        assertThat(goalResult.getUser()).isEqualTo(user);
        assertThat(goalResult.getCategory()).isEqualTo(category);
        assertThat(goalResult.getDate()).isEqualTo(date);
        assertThat(goalResult.getPeriodType()).isEqualTo(periodType);
        assertThat(goalResult.getGoalSeconds()).isEqualTo(goalSeconds);
        assertThat(goalResult.getAchievedSeconds()).isEqualTo(achievedSeconds);
        assertThat(goalResult.getId()).isNull(); // 아직 영속화되지 않음
    }

    @Test
    @DisplayName("Builder로 주간 목표 결과 생성")
    void shouldCreateWeeklyGoalResultWithBuilder() {
        // given
        User user = new User("user456", "주간테스트사용자");
        String category = "디자인";
        LocalDate date = LocalDate.of(2024, 1, 8); // 주의 첫째 날
        PeriodType periodType = PeriodType.WEEKLY;
        int goalSeconds = 50400; // 14시간
        int achievedSeconds = 43200; // 12시간

        // when
        GoalResult goalResult = GoalResult.builder()
                .user(user)
                .category(category)
                .date(date)
                .periodType(periodType)
                .goalSeconds(goalSeconds)
                .achievedSeconds(achievedSeconds)
                .build();

        // then
        assertThat(goalResult.getUser()).isEqualTo(user);
        assertThat(goalResult.getCategory()).isEqualTo(category);
        assertThat(goalResult.getDate()).isEqualTo(date);
        assertThat(goalResult.getPeriodType()).isEqualTo(PeriodType.WEEKLY);
        assertThat(goalResult.getGoalSeconds()).isEqualTo(50400);
        assertThat(goalResult.getAchievedSeconds()).isEqualTo(43200);
    }

    @Test
    @DisplayName("Builder로 월간 목표 결과 생성")
    void shouldCreateMonthlyGoalResultWithBuilder() {
        // given
        User user = new User("user789", "월간테스트사용자");
        String category = "업무";
        LocalDate date = LocalDate.of(2024, 1, 1); // 월의 첫째 날
        PeriodType periodType = PeriodType.MONTHLY;
        int goalSeconds = 216000; // 60시간
        int achievedSeconds = 180000; // 50시간

        // when
        GoalResult goalResult = GoalResult.builder()
                .user(user)
                .category(category)
                .date(date)
                .periodType(periodType)
                .goalSeconds(goalSeconds)
                .achievedSeconds(achievedSeconds)
                .build();

        // then
        assertThat(goalResult.getPeriodType()).isEqualTo(PeriodType.MONTHLY);
        assertThat(goalResult.getGoalSeconds()).isEqualTo(216000);
        assertThat(goalResult.getAchievedSeconds()).isEqualTo(180000);
        assertThat(goalResult.getCategory()).isEqualTo("업무");
    }

    @Test
    @DisplayName("목표 달성률 계산 시나리오")
    void shouldCalculateAchievementRate() {
        // given
        User user = new User("user123", "테스트사용자");

        // 목표 초과 달성
        GoalResult overAchieved = GoalResult.builder()
                .user(user)
                .category("개발")
                .date(LocalDate.now())
                .periodType(PeriodType.DAILY)
                .goalSeconds(3600) // 1시간 목표
                .achievedSeconds(5400) // 1.5시간 달성
                .build();

        // 목표 정확 달성
        GoalResult exactAchieved = GoalResult.builder()
                .user(user)
                .category("디자인")
                .date(LocalDate.now())
                .periodType(PeriodType.DAILY)
                .goalSeconds(7200) // 2시간 목표
                .achievedSeconds(7200) // 2시간 달성
                .build();

        // 목표 미달성
        GoalResult underAchieved = GoalResult.builder()
                .user(user)
                .category("업무")
                .date(LocalDate.now())
                .periodType(PeriodType.DAILY)
                .goalSeconds(10800) // 3시간 목표
                .achievedSeconds(7200) // 2시간만 달성
                .build();

        // when & then
        double overRate = (double) overAchieved.getAchievedSeconds() / overAchieved.getGoalSeconds();
        double exactRate = (double) exactAchieved.getAchievedSeconds() / exactAchieved.getGoalSeconds();
        double underRate = (double) underAchieved.getAchievedSeconds() / underAchieved.getGoalSeconds();

        assertThat(overRate).isEqualTo(1.5); // 150% 달성
        assertThat(exactRate).isEqualTo(1.0); // 100% 달성
        assertThat(underRate).isCloseTo(0.67, org.assertj.core.data.Offset.offset(0.01)); // 약 67% 달성
    }

    @Test
    @DisplayName("0초 목표와 달성 처리")
    void shouldHandleZeroSecondsGoalAndAchievement() {
        // given
        User user = new User("user123", "테스트사용자");

        // when
        GoalResult zeroGoal = GoalResult.builder()
                .user(user)
                .category("휴식")
                .date(LocalDate.now())
                .periodType(PeriodType.DAILY)
                .goalSeconds(0)
                .achievedSeconds(0)
                .build();

        GoalResult zeroAchievement = GoalResult.builder()
                .user(user)
                .category("학습")
                .date(LocalDate.now())
                .periodType(PeriodType.DAILY)
                .goalSeconds(3600) // 1시간 목표
                .achievedSeconds(0) // 0초 달성
                .build();

        // then
        assertThat(zeroGoal.getGoalSeconds()).isEqualTo(0);
        assertThat(zeroGoal.getAchievedSeconds()).isEqualTo(0);

        assertThat(zeroAchievement.getGoalSeconds()).isEqualTo(3600);
        assertThat(zeroAchievement.getAchievedSeconds()).isEqualTo(0);
    }

    @Test
    @DisplayName("다양한 PeriodType별 목표 결과 생성")
    void shouldCreateGoalResultsWithDifferentPeriodTypes() {
        // given
        User user = new User("user123", "테스트사용자");
        String category = "개발";
        LocalDate date = LocalDate.of(2024, 1, 1);

        // when
        GoalResult dailyGoal = GoalResult.builder()
                .user(user)
                .category(category)
                .date(date)
                .periodType(PeriodType.DAILY)
                .goalSeconds(7200)
                .achievedSeconds(6000)
                .build();

        GoalResult weeklyGoal = GoalResult.builder()
                .user(user)
                .category(category)
                .date(date)
                .periodType(PeriodType.WEEKLY)
                .goalSeconds(50400)
                .achievedSeconds(45000)
                .build();

        GoalResult monthlyGoal = GoalResult.builder()
                .user(user)
                .category(category)
                .date(date)
                .periodType(PeriodType.MONTHLY)
                .goalSeconds(216000)
                .achievedSeconds(200000)
                .build();

        // then
        assertThat(dailyGoal.getPeriodType()).isEqualTo(PeriodType.DAILY);
        assertThat(weeklyGoal.getPeriodType()).isEqualTo(PeriodType.WEEKLY);
        assertThat(monthlyGoal.getPeriodType()).isEqualTo(PeriodType.MONTHLY);

        // 모든 목표가 같은 사용자, 카테고리, 날짜를 가지지만 다른 기간 타입을 가짐
        assertThat(dailyGoal.getUser()).isEqualTo(weeklyGoal.getUser());
        assertThat(weeklyGoal.getUser()).isEqualTo(monthlyGoal.getUser());
        assertThat(dailyGoal.getCategory()).isEqualTo(weeklyGoal.getCategory());
        assertThat(weeklyGoal.getCategory()).isEqualTo(monthlyGoal.getCategory());
        assertThat(dailyGoal.getDate()).isEqualTo(weeklyGoal.getDate());
        assertThat(weeklyGoal.getDate()).isEqualTo(monthlyGoal.getDate());
    }

    @Test
    @DisplayName("대용량 시간 값 처리")
    void shouldHandleLargeTimeValues() {
        // given
        User user = new User("user123", "테스트사용자");
        int largeGoalSeconds = 86400 * 30; // 30일 = 2,592,000초
        int largeAchievedSeconds = 86400 * 25; // 25일 = 2,160,000초

        // when
        GoalResult goalResult = GoalResult.builder()
                .user(user)
                .category("장기프로젝트")
                .date(LocalDate.of(2024, 1, 1))
                .periodType(PeriodType.MONTHLY)
                .goalSeconds(largeGoalSeconds)
                .achievedSeconds(largeAchievedSeconds)
                .build();

        // then
        assertThat(goalResult.getGoalSeconds()).isEqualTo(2592000);
        assertThat(goalResult.getAchievedSeconds()).isEqualTo(2160000);

        double achievementRate = (double) goalResult.getAchievedSeconds() / goalResult.getGoalSeconds();
        assertThat(achievementRate).isCloseTo(0.833, org.assertj.core.data.Offset.offset(0.001)); // 약 83.3%
    }
}