package com.swmStrong.demo.domain.pomodoro.entity;

import org.bson.types.ObjectId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("PomodoroUsageLog 엔티티 테스트")
class PomodoroUsageLogTest {

    @Test
    @DisplayName("Builder로 PomodoroUsageLog 생성")
    void shouldCreatePomodoroUsageLogWithBuilder() {
        // given
        String userId = "user123";
        ObjectId categorizedDataId = new ObjectId();
        int session = 1;
        int sessionMinutes = 25;
        LocalDate sessionDate = LocalDate.now();
        ObjectId categoryId = new ObjectId();
        double timestamp = 1640995200.0;
        double duration = 1500.0;

        // when
        PomodoroUsageLog pomodoroUsageLog = PomodoroUsageLog.builder()
                .userId(userId)
                .CategorizedDataId(categorizedDataId)
                .session(session)
                .sessionMinutes(sessionMinutes)
                .sessionDate(sessionDate)
                .categoryId(categoryId)
                .timestamp(timestamp)
                .duration(duration)
                .build();

        // then
        assertThat(pomodoroUsageLog.getUserId()).isEqualTo(userId);
        assertThat(pomodoroUsageLog.getCategorizedDataId()).isEqualTo(categorizedDataId);
        assertThat(pomodoroUsageLog.getSession()).isEqualTo(session);
        assertThat(pomodoroUsageLog.getSessionMinutes()).isEqualTo(sessionMinutes);
        assertThat(pomodoroUsageLog.getSessionDate()).isEqualTo(sessionDate);
        assertThat(pomodoroUsageLog.getCategoryId()).isEqualTo(categoryId);
        assertThat(pomodoroUsageLog.getTimestamp()).isEqualTo(timestamp);
        assertThat(pomodoroUsageLog.getDuration()).isEqualTo(duration);
    }

    @Test
    @DisplayName("카테고리 ID 업데이트")
    void shouldUpdateCategoryId() {
        // given
        PomodoroUsageLog pomodoroUsageLog = PomodoroUsageLog.builder()
                .userId("user123")
                .CategorizedDataId(new ObjectId())
                .session(1)
                .sessionMinutes(25)
                .sessionDate(LocalDate.now())
                .categoryId(new ObjectId())
                .timestamp(1640995200.0)
                .duration(1500.0)
                .build();
        ObjectId newCategoryId = new ObjectId();

        // when
        pomodoroUsageLog.updateCategoryId(newCategoryId);

        // then
        assertThat(pomodoroUsageLog.getCategoryId()).isEqualTo(newCategoryId);
    }

    @Test
    @DisplayName("CategorizedData ID 업데이트")
    void shouldUpdateCategorizedDataId() {
        // given
        PomodoroUsageLog pomodoroUsageLog = PomodoroUsageLog.builder()
                .userId("user123")
                .CategorizedDataId(new ObjectId())
                .session(1)
                .sessionMinutes(25)
                .sessionDate(LocalDate.now())
                .categoryId(new ObjectId())
                .timestamp(1640995200.0)
                .duration(1500.0)
                .build();
        ObjectId newCategorizedDataId = new ObjectId();

        // when
        pomodoroUsageLog.updateCategorizedDataId(newCategorizedDataId);

        // then
        assertThat(pomodoroUsageLog.getCategorizedDataId()).isEqualTo(newCategorizedDataId);
    }

    @Test
    @DisplayName("복합 업데이트 시나리오")
    void shouldHandleComplexUpdateScenario() {
        // given
        PomodoroUsageLog pomodoroUsageLog = PomodoroUsageLog.builder()
                .userId("user123")
                .CategorizedDataId(new ObjectId())
                .session(1)
                .sessionMinutes(25)
                .sessionDate(LocalDate.of(2024, 1, 1))
                .categoryId(new ObjectId())
                .timestamp(1640995200.0)
                .duration(1500.0)
                .build();

        ObjectId newCategoryId = new ObjectId();
        ObjectId newCategorizedDataId = new ObjectId();

        // when
        pomodoroUsageLog.updateCategoryId(newCategoryId);
        pomodoroUsageLog.updateCategorizedDataId(newCategorizedDataId);

        // then
        assertThat(pomodoroUsageLog.getCategoryId()).isEqualTo(newCategoryId);
        assertThat(pomodoroUsageLog.getCategorizedDataId()).isEqualTo(newCategorizedDataId);
        assertThat(pomodoroUsageLog.getUserId()).isEqualTo("user123");
        assertThat(pomodoroUsageLog.getSession()).isEqualTo(1);
    }
}