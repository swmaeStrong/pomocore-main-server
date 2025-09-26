package com.swmStrong.demo.domain.leaderboard.entity;

import com.swmStrong.demo.domain.common.enums.PeriodType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Leaderboard 엔티티 테스트")
class LeaderboardTest {

    @Test
    @DisplayName("Builder로 일일 Leaderboard 생성")
    void shouldCreateDailyLeaderboardWithBuilder() {
        // given
        String userId = "user123";
        String categoryId = "category456";
        PeriodType periodType = PeriodType.DAILY;
        String periodKey = "2024-01-01";
        Integer ranking = 1;
        double score = 85.5;

        // when
        Leaderboard leaderboard = Leaderboard.builder()
                .userId(userId)
                .categoryId(categoryId)
                .periodType(periodType)
                .periodKey(periodKey)
                .ranking(ranking)
                .score(score)
                .build();

        // then
        assertThat(leaderboard.getId()).isNotNull();
        assertThat(leaderboard.getUserId()).isEqualTo(userId);
        assertThat(leaderboard.getCategoryId()).isEqualTo(categoryId);
        assertThat(leaderboard.getPeriodType()).isEqualTo(periodType);
        assertThat(leaderboard.getPeriodKey()).isEqualTo(periodKey);
        assertThat(leaderboard.getRanking()).isEqualTo(ranking);
        assertThat(leaderboard.getScore()).isEqualTo(score);
        assertThat(leaderboard.getCreatedAt()).isNotNull();
        assertThat(leaderboard.getCreatedAt()).isBefore(LocalDateTime.now().plusSeconds(1));
    }

    @Test
    @DisplayName("Builder로 주간 Leaderboard 생성")
    void shouldCreateWeeklyLeaderboardWithBuilder() {
        // given
        String userId = "user456";
        String categoryId = "category789";
        PeriodType periodType = PeriodType.WEEKLY;
        String periodKey = "2024-W01";
        Integer ranking = 3;
        double score = 78.2;

        // when
        Leaderboard leaderboard = Leaderboard.builder()
                .userId(userId)
                .categoryId(categoryId)
                .periodType(periodType)
                .periodKey(periodKey)
                .ranking(ranking)
                .score(score)
                .build();

        // then
        assertThat(leaderboard.getId()).isNotNull();
        assertThat(leaderboard.getUserId()).isEqualTo(userId);
        assertThat(leaderboard.getCategoryId()).isEqualTo(categoryId);
        assertThat(leaderboard.getPeriodType()).isEqualTo(periodType);
        assertThat(leaderboard.getPeriodKey()).isEqualTo(periodKey);
        assertThat(leaderboard.getRanking()).isEqualTo(ranking);
        assertThat(leaderboard.getScore()).isEqualTo(score);
    }

    @Test
    @DisplayName("Builder로 월간 Leaderboard 생성")
    void shouldCreateMonthlyLeaderboardWithBuilder() {
        // given
        String userId = "user789";
        String categoryId = "category123";
        PeriodType periodType = PeriodType.MONTHLY;
        String periodKey = "2024-01";
        Integer ranking = 5;
        double score = 92.8;

        // when
        Leaderboard leaderboard = Leaderboard.builder()
                .userId(userId)
                .categoryId(categoryId)
                .periodType(periodType)
                .periodKey(periodKey)
                .ranking(ranking)
                .score(score)
                .build();

        // then
        assertThat(leaderboard.getPeriodType()).isEqualTo(PeriodType.MONTHLY);
        assertThat(leaderboard.getPeriodKey()).isEqualTo("2024-01");
        assertThat(leaderboard.getRanking()).isEqualTo(5);
        assertThat(leaderboard.getScore()).isEqualTo(92.8);
    }

    @Test
    @DisplayName("ID 자동 생성 확인")
    void shouldGenerateUniqueIds() {
        // given
        String userId = "user123";
        String categoryId = "category456";
        PeriodType periodType = PeriodType.DAILY;
        String periodKey = "2024-01-01";

        // when
        Leaderboard leaderboard1 = Leaderboard.builder()
                .userId(userId)
                .categoryId(categoryId)
                .periodType(periodType)
                .periodKey(periodKey)
                .ranking(1)
                .score(85.0)
                .build();

        Leaderboard leaderboard2 = Leaderboard.builder()
                .userId(userId)
                .categoryId(categoryId)
                .periodType(periodType)
                .periodKey(periodKey)
                .ranking(2)
                .score(84.0)
                .build();

        // then
        assertThat(leaderboard1.getId()).isNotNull();
        assertThat(leaderboard2.getId()).isNotNull();
        assertThat(leaderboard1.getId()).isNotEqualTo(leaderboard2.getId());
        assertThat(leaderboard1.getId()).matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    }

    @Test
    @DisplayName("생성시간 자동 설정 확인")
    void shouldSetCreatedAtAutomatically() {
        // given
        LocalDateTime beforeCreation = LocalDateTime.now();

        // when
        Leaderboard leaderboard = Leaderboard.builder()
                .userId("user123")
                .categoryId("category456")
                .periodType(PeriodType.DAILY)
                .periodKey("2024-01-01")
                .ranking(1)
                .score(85.0)
                .build();

        LocalDateTime afterCreation = LocalDateTime.now();

        // then
        assertThat(leaderboard.getCreatedAt()).isNotNull();
        assertThat(leaderboard.getCreatedAt()).isAfter(beforeCreation.minusSeconds(1));
        assertThat(leaderboard.getCreatedAt()).isBefore(afterCreation.plusSeconds(1));
    }

    @Test
    @DisplayName("다양한 PeriodType별 Leaderboard 생성")
    void shouldCreateLeaderboardsWithDifferentPeriodTypes() {
        // given
        String userId = "user123";
        String categoryId = "category456";

        // when
        Leaderboard dailyLeaderboard = Leaderboard.builder()
                .userId(userId)
                .categoryId(categoryId)
                .periodType(PeriodType.DAILY)
                .periodKey("2024-01-01")
                .ranking(1)
                .score(90.0)
                .build();

        Leaderboard weeklyLeaderboard = Leaderboard.builder()
                .userId(userId)
                .categoryId(categoryId)
                .periodType(PeriodType.WEEKLY)
                .periodKey("2024-W01")
                .ranking(2)
                .score(88.5)
                .build();

        Leaderboard monthlyLeaderboard = Leaderboard.builder()
                .userId(userId)
                .categoryId(categoryId)
                .periodType(PeriodType.MONTHLY)
                .periodKey("2024-01")
                .ranking(3)
                .score(87.2)
                .build();

        // then
        assertThat(dailyLeaderboard.getPeriodType()).isEqualTo(PeriodType.DAILY);
        assertThat(weeklyLeaderboard.getPeriodType()).isEqualTo(PeriodType.WEEKLY);
        assertThat(monthlyLeaderboard.getPeriodType()).isEqualTo(PeriodType.MONTHLY);

        assertThat(dailyLeaderboard.getPeriodKey()).isEqualTo("2024-01-01");
        assertThat(weeklyLeaderboard.getPeriodKey()).isEqualTo("2024-W01");
        assertThat(monthlyLeaderboard.getPeriodKey()).isEqualTo("2024-01");

        assertThat(dailyLeaderboard.getRanking()).isEqualTo(1);
        assertThat(weeklyLeaderboard.getRanking()).isEqualTo(2);
        assertThat(monthlyLeaderboard.getRanking()).isEqualTo(3);
    }

    @Test
    @DisplayName("소수점 점수 처리 확인")
    void shouldHandleDecimalScores() {
        // given
        double preciseScore = 87.12345;

        // when
        Leaderboard leaderboard = Leaderboard.builder()
                .userId("user123")
                .categoryId("category456")
                .periodType(PeriodType.DAILY)
                .periodKey("2024-01-01")
                .ranking(1)
                .score(preciseScore)
                .build();

        // then
        assertThat(leaderboard.getScore()).isEqualTo(preciseScore);
        assertThat(leaderboard.getScore()).isEqualTo(87.12345);
    }
}