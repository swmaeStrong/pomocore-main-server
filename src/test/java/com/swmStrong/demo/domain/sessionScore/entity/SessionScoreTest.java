package com.swmStrong.demo.domain.sessionScore.entity;

import com.swmStrong.demo.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SessionScore 엔티티 테스트")
class SessionScoreTest {

    @Test
    @DisplayName("Builder로 SessionScore 생성")
    void shouldCreateSessionScoreWithBuilder() {
        // given
        User user = new User("user123", "테스트사용자");
        double timestamp = 1640995200.0;
        double duration = 1500.0;
        int sessionMinutes = 25;
        LocalDate sessionDate = LocalDate.of(2024, 1, 1);
        int session = 1;
        int distractedCount = 5;
        int distractedDuration = 300;
        double afkDuration = 120.0;

        // when
        SessionScore sessionScore = SessionScore.builder()
                .user(user)
                .timestamp(timestamp)
                .duration(duration)
                .sessionMinutes(sessionMinutes)
                .sessionDate(sessionDate)
                .session(session)
                .distractedCount(distractedCount)
                .distractedDuration(distractedDuration)
                .afkDuration(afkDuration)
                .build();

        // then
        assertThat(sessionScore.getUser()).isEqualTo(user);
        assertThat(sessionScore.getTimestamp()).isEqualTo(timestamp);
        assertThat(sessionScore.getDuration()).isEqualTo(duration);
        assertThat(sessionScore.getSessionMinutes()).isEqualTo(sessionMinutes);
        assertThat(sessionScore.getSessionDate()).isEqualTo(sessionDate);
        assertThat(sessionScore.getSession()).isEqualTo(session);
        assertThat(sessionScore.getDistractedCount()).isEqualTo(distractedCount);
        assertThat(sessionScore.getDistractedDuration()).isEqualTo(distractedDuration);
        assertThat(sessionScore.getAfkDuration()).isEqualTo(afkDuration);
        assertThat(sessionScore.getTitle()).isNull();
        assertThat(sessionScore.getTitleEng()).isNull();
    }

    @Test
    @DisplayName("세션 상세정보 업데이트")
    void shouldUpdateDetails() {
        // given
        User user = new User("user123", "테스트사용자");
        SessionScore sessionScore = SessionScore.builder()
                .user(user)
                .timestamp(1640995200.0)
                .duration(1500.0)
                .sessionMinutes(25)
                .sessionDate(LocalDate.now())
                .session(1)
                .distractedCount(5)
                .distractedDuration(300)
                .afkDuration(120.0)
                .build();

        double newTimestamp = 1641000000.0;
        double newAfkDuration = 200.0;
        double newDuration = 1800.0;
        int newDistractedCount = 8;
        int newDistractedDuration = 400;

        // when
        sessionScore.updateDetails(newTimestamp, newAfkDuration, newDuration, newDistractedCount, newDistractedDuration);

        // then
        assertThat(sessionScore.getTimestamp()).isEqualTo(newTimestamp);
        assertThat(sessionScore.getAfkDuration()).isEqualTo(newAfkDuration);
        assertThat(sessionScore.getDuration()).isEqualTo(newDuration);
        assertThat(sessionScore.getDistractedCount()).isEqualTo(newDistractedCount);
        assertThat(sessionScore.getDistractedDuration()).isEqualTo(newDistractedDuration);
    }

    @Test
    @DisplayName("타이틀 업데이트")
    void shouldUpdateTitle() {
        // given
        User user = new User("user123", "테스트사용자");
        SessionScore sessionScore = SessionScore.builder()
                .user(user)
                .timestamp(1640995200.0)
                .duration(1500.0)
                .sessionMinutes(25)
                .sessionDate(LocalDate.now())
                .session(1)
                .distractedCount(5)
                .distractedDuration(300)
                .afkDuration(120.0)
                .build();
        String title = "집중 세션 제목";

        // when
        sessionScore.updateTitle(title);

        // then
        assertThat(sessionScore.getTitle()).isEqualTo(title);
    }

    @Test
    @DisplayName("영어 타이틀 업데이트")
    void shouldUpdateEngTitle() {
        // given
        User user = new User("user123", "테스트사용자");
        SessionScore sessionScore = SessionScore.builder()
                .user(user)
                .timestamp(1640995200.0)
                .duration(1500.0)
                .sessionMinutes(25)
                .sessionDate(LocalDate.now())
                .session(1)
                .distractedCount(5)
                .distractedDuration(300)
                .afkDuration(120.0)
                .build();
        String titleEng = "Focus Session Title";

        // when
        sessionScore.updateEngTitle(titleEng);

        // then
        assertThat(sessionScore.getTitleEng()).isEqualTo(titleEng);
    }

    @Test
    @DisplayName("복합 업데이트 시나리오")
    void shouldHandleComplexUpdateScenario() {
        // given
        User user = new User("user123", "테스트사용자");
        SessionScore sessionScore = SessionScore.builder()
                .user(user)
                .timestamp(1640995200.0)
                .duration(1500.0)
                .sessionMinutes(25)
                .sessionDate(LocalDate.of(2024, 1, 1))
                .session(1)
                .distractedCount(5)
                .distractedDuration(300)
                .afkDuration(120.0)
                .build();

        // when
        sessionScore.updateTitle("집중 세션");
        sessionScore.updateEngTitle("Focus Session");
        sessionScore.updateDetails(1641000000.0, 180.0, 1800.0, 3, 200);

        // then
        assertThat(sessionScore.getTitle()).isEqualTo("집중 세션");
        assertThat(sessionScore.getTitleEng()).isEqualTo("Focus Session");
        assertThat(sessionScore.getTimestamp()).isEqualTo(1641000000.0);
        assertThat(sessionScore.getAfkDuration()).isEqualTo(180.0);
        assertThat(sessionScore.getDuration()).isEqualTo(1800.0);
        assertThat(sessionScore.getDistractedCount()).isEqualTo(3);
        assertThat(sessionScore.getDistractedDuration()).isEqualTo(200);
        assertThat(sessionScore.getUser().getId()).isEqualTo("user123");
        assertThat(sessionScore.getSessionMinutes()).isEqualTo(25);
        assertThat(sessionScore.getSession()).isEqualTo(1);
    }

    @Test
    @DisplayName("동일한 사용자, 날짜, 세션 번호로 생성된 SessionScore 비교")
    void shouldCompareSessionScoreWithSameUserDateAndSession() {
        // given
        User user = new User("user123", "테스트사용자");
        LocalDate sessionDate = LocalDate.of(2024, 1, 1);
        int session = 1;

        SessionScore sessionScore1 = SessionScore.builder()
                .user(user)
                .timestamp(1640995200.0)
                .duration(1500.0)
                .sessionMinutes(25)
                .sessionDate(sessionDate)
                .session(session)
                .distractedCount(5)
                .distractedDuration(300)
                .afkDuration(120.0)
                .build();

        SessionScore sessionScore2 = SessionScore.builder()
                .user(user)
                .timestamp(1641000000.0)
                .duration(1800.0)
                .sessionMinutes(30)
                .sessionDate(sessionDate)
                .session(session)
                .distractedCount(3)
                .distractedDuration(200)
                .afkDuration(100.0)
                .build();

        // then
        assertThat(sessionScore1.getUser()).isEqualTo(sessionScore2.getUser());
        assertThat(sessionScore1.getSessionDate()).isEqualTo(sessionScore2.getSessionDate());
        assertThat(sessionScore1.getSession()).isEqualTo(sessionScore2.getSession());
        assertThat(sessionScore1).isNotEqualTo(sessionScore2); // 다른 인스턴스
    }
}