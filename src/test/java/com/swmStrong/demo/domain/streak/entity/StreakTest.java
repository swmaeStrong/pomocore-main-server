package com.swmStrong.demo.domain.streak.entity;

import com.swmStrong.demo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Streak 엔티티 테스트")
class StreakTest {

    @Test
    @DisplayName("User로 Streak 생성")
    void shouldCreateStreakWithUser() {
        // given
        User user = new User("test123", "테스트닉네임");

        // when
        Streak streak = Streak.builder()
                .user(user)
                .build();

        // then
        assertThat(streak.getUser()).isEqualTo(user);
        assertThat(streak.getCurrentStreak()).isEqualTo(0);
        assertThat(streak.getMaxStreak()).isEqualTo(0);
        assertThat(streak.getLastActiveDate()).isNull();
    }

    @Test
    @DisplayName("현재 스트릭 초기화")
    void shouldResetCurrentStreak() {
        // given
        User user = new User("test123", "테스트닉네임");
        Streak streak = Streak.builder()
                .user(user)
                .build();
        streak.plusStreak();
        streak.plusStreak();

        // when
        streak.resetCurrentStreak();

        // then
        assertThat(streak.getCurrentStreak()).isEqualTo(0);
        assertThat(streak.getMaxStreak()).isEqualTo(2); // 최대 스트릭은 유지
    }

    @Test
    @DisplayName("스트릭 증가 - 현재 스트릭이 최대 스트릭보다 작은 경우")
    void shouldIncreaseStreakWhenCurrentIsLessThanMax() {
        // given
        User user = new User("test123", "테스트닉네임");
        Streak streak = Streak.builder()
                .user(user)
                .build();

        // when
        streak.plusStreak();

        // then
        assertThat(streak.getCurrentStreak()).isEqualTo(1);
        assertThat(streak.getMaxStreak()).isEqualTo(1);
    }

    @Test
    @DisplayName("스트릭 증가 - 현재 스트릭이 최대 스트릭과 같은 경우")
    void shouldIncreaseStreakAndMaxStreakWhenCurrentEqualsMax() {
        // given
        User user = new User("test123", "테스트닉네임");
        Streak streak = Streak.builder()
                .user(user)
                .build();
        streak.plusStreak(); // current: 1, max: 1

        // when
        streak.plusStreak();

        // then
        assertThat(streak.getCurrentStreak()).isEqualTo(2);
        assertThat(streak.getMaxStreak()).isEqualTo(2);
    }

    @Test
    @DisplayName("최종 활성 날짜 갱신")
    void shouldRenewLastActiveDate() {
        // given
        User user = new User("test123", "테스트닉네임");
        Streak streak = Streak.builder()
                .user(user)
                .build();
        LocalDate today = LocalDate.now();

        // when
        streak.renewLastActiveDate(today);

        // then
        assertThat(streak.getLastActiveDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("복합 시나리오 - 스트릭 증가 후 초기화")
    void shouldHandleComplexScenario() {
        // given
        User user = new User("test123", "테스트닉네임");
        Streak streak = Streak.builder()
                .user(user)
                .build();
        LocalDate date = LocalDate.of(2024, 1, 1);

        // when
        streak.plusStreak(); // current: 1, max: 1
        streak.plusStreak(); // current: 2, max: 2
        streak.plusStreak(); // current: 3, max: 3
        streak.renewLastActiveDate(date);
        streak.resetCurrentStreak(); // current: 0, max: 3 (유지)

        // then
        assertThat(streak.getCurrentStreak()).isEqualTo(0);
        assertThat(streak.getMaxStreak()).isEqualTo(3);
        assertThat(streak.getLastActiveDate()).isEqualTo(date);
    }
}