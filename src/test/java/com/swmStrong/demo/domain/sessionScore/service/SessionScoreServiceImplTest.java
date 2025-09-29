package com.swmStrong.demo.domain.sessionScore.service;

import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.facade.PomodoroSessionProvider;
import com.swmStrong.demo.domain.sessionScore.dto.SessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.dto.WeeklySessionScoreResponseDto;
import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import com.swmStrong.demo.domain.user.entity.User;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SessionScoreServiceImpl 테스트")
class SessionScoreServiceImplTest {

    @Mock
    private SessionScoreRepository sessionScoreRepository;

    @Mock
    private PomodoroSessionProvider pomodoroSessionProvider;

    @Mock
    private CategoryProvider categoryProvider;

    @Mock
    private LeaderboardProvider leaderboardProvider;

    @Mock
    private SessionStateManager sessionStateManager;

    @Mock
    private SessionPollingManager sessionPollingManager;

    @InjectMocks
    private SessionScoreServiceImpl sessionScoreService;

    private User testUser;
    private String userId;
    private LocalDate testDate;
    private ObjectId categoryId1;
    private ObjectId categoryId2;

    @BeforeEach
    void setUp() {
        userId = "user123";
        testUser = new User(userId, "테스트사용자");
        testDate = LocalDate.of(2024, 1, 15);
        categoryId1 = new ObjectId();
        categoryId2 = new ObjectId();
    }

    @Test
    @DisplayName("사용자별 날짜별 세션 점수 조회")
    void shouldGetSessionScoresByUserIdAndDate() {
        // given
        SessionScore sessionScore1 = SessionScore.builder()
                .user(testUser)
                .sessionDate(testDate)
                .session(1)
                .sessionMinutes(25)
                .timestamp(1640995200.0)
                .duration(1500.0)
                .distractedCount(3)
                .distractedDuration(120)
                .afkDuration(60.0)
                .build();

        SessionScore sessionScore2 = SessionScore.builder()
                .user(testUser)
                .sessionDate(testDate)
                .session(2)
                .sessionMinutes(25)
                .timestamp(1640997000.0)
                .duration(1450.0)
                .distractedCount(2)
                .distractedDuration(80)
                .afkDuration(30.0)
                .build();

        List<SessionScore> sessionScores = Arrays.asList(sessionScore1, sessionScore2);

        PomodoroUsageLog usageLog1 = PomodoroUsageLog.builder()
                .userId(userId)
                .categoryId(categoryId1)
                .sessionDate(testDate)
                .session(1)
                .timestamp(1640995200.0)
                .duration(800.0)
                .build();

        PomodoroUsageLog usageLog2 = PomodoroUsageLog.builder()
                .userId(userId)
                .categoryId(categoryId2)
                .sessionDate(testDate)
                .session(1)
                .timestamp(1640995400.0)
                .duration(700.0)
                .build();

        Map<ObjectId, String> categoryMap = new HashMap<>();
        categoryMap.put(categoryId1, "개발");
        categoryMap.put(categoryId2, "학습");

        when(sessionScoreRepository.findByUserIdAndSessionDate(userId, testDate))
                .thenReturn(sessionScores);
        when(categoryProvider.getCategoryMapById()).thenReturn(categoryMap);
        when(pomodoroSessionProvider.loadByUserIdAndSessionAndSessionDate(userId, 1, testDate))
                .thenReturn(Arrays.asList(usageLog1, usageLog2));
        when(pomodoroSessionProvider.loadByUserIdAndSessionAndSessionDate(userId, 2, testDate))
                .thenReturn(Arrays.asList());

        // when
        List<SessionScoreResponseDto> result = sessionScoreService.getByUserIdAndSessionDate(userId, testDate);

        // then
        assertThat(result).hasSize(2);

        SessionScoreResponseDto firstSession = result.get(0);
        assertThat(firstSession.session()).isEqualTo(1);
        assertThat(firstSession.sessionMinutes()).isEqualTo(25);
        assertThat(firstSession.details()).hasSize(2);
        assertThat(firstSession.details().get(0).category()).isEqualTo("개발");
        assertThat(firstSession.details().get(0).duration()).isEqualTo(800.0);
        assertThat(firstSession.details().get(1).category()).isEqualTo("학습");
        assertThat(firstSession.details().get(1).duration()).isEqualTo(700.0);

        SessionScoreResponseDto secondSession = result.get(1);
        assertThat(secondSession.session()).isEqualTo(2);
        assertThat(secondSession.details()).isEmpty();
    }

    @Test
    @DisplayName("주간 세션 점수 상세 조회")
    void shouldGetWeeklySessionDetails() {
        // given
        LocalDate weekDate = LocalDate.of(2024, 1, 10); // 수요일
        LocalDate startOfWeek = LocalDate.of(2024, 1, 8).minusDays(1); // 주 시작 계산
        LocalDate endOfWeek = LocalDate.of(2024, 1, 14).plusDays(1); // 주 끝 계산

        SessionScore sessionScore1 = SessionScore.builder()
                .user(testUser)
                .sessionDate(LocalDate.of(2024, 1, 8))
                .session(1)
                .sessionMinutes(25)
                .timestamp(1640995200.0)
                .duration(1500.0)
                .distractedCount(2)
                .distractedDuration(100)
                .afkDuration(50.0)
                .build();

        SessionScore sessionScore2 = SessionScore.builder()
                .user(testUser)
                .sessionDate(LocalDate.of(2024, 1, 10))
                .session(1)
                .sessionMinutes(25)
                .timestamp(1641081600.0)
                .duration(1400.0)
                .distractedCount(1)
                .distractedDuration(60)
                .afkDuration(30.0)
                .build();

        List<SessionScore> weeklyScores = Arrays.asList(sessionScore1, sessionScore2);

        when(sessionScoreRepository.findByUserIdAndSessionDateBetween(userId, startOfWeek, endOfWeek))
                .thenReturn(weeklyScores);

        // when
        WeeklySessionScoreResponseDto result = sessionScoreService.getWeeklyDetailsByUserIdAndSessionDate(userId, weekDate);

        // then
        assertThat(result.avgScore()).isGreaterThan(0);
    }

    @Test
    @DisplayName("세션 대시보드 점수 조회")
    void shouldGetScoreByUserIdAndSessionDate() {
        // given
        SessionScore sessionScore = SessionScore.builder()
                .user(testUser)
                .sessionDate(testDate)
                .session(1)
                .sessionMinutes(25)
                .timestamp(1640995200.0)
                .duration(1500.0)
                .distractedCount(3)
                .distractedDuration(120)
                .afkDuration(60.0)
                .build();

        when(sessionScoreRepository.findAllByUserIdAndSessionDate(userId, testDate))
                .thenReturn(List.of(sessionScore));

        // when
        var result = sessionScoreService.getScoreByUserIdAndSessionDate(userId, testDate);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).session()).isEqualTo(1);
        assertThat(result.get(0).sessionMinutes()).isEqualTo(25);
        assertThat(result.get(0).score()).isGreaterThan(0);
        assertThat(result.get(0).duration()).isEqualTo(1500.0);
    }

    @Test
    @DisplayName("빈 세션 대시보드 조회")
    void shouldReturnEmptySessionDashboard() {
        // given
        when(sessionScoreRepository.findAllByUserIdAndSessionDate(userId, testDate))
                .thenReturn(List.of());

        // when
        var result = sessionScoreService.getScoreByUserIdAndSessionDate(userId, testDate);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("빈 세션 점수 목록 조회")
    void shouldReturnEmptySessionScoresList() {
        // given
        when(sessionScoreRepository.findAllByUserIdAndSessionDate(userId, testDate))
                .thenReturn(List.of());
        when(categoryProvider.getCategoryMapById()).thenReturn(new HashMap<>());

        // when
        List<SessionScoreResponseDto> result = sessionScoreService.getByUserIdAndSessionDate(userId, testDate);

        // then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("점수 계산 로직 검증")
    void shouldCalculateScoreCorrectly() {
        // given
        SessionScore highScore = SessionScore.builder()
                .user(testUser)
                .sessionDate(testDate)
                .session(1)
                .sessionMinutes(25)
                .timestamp(1640995200.0)
                .duration(1500.0) // 25분 = 1500초
                .distractedCount(1)
                .distractedDuration(30)
                .afkDuration(10.0)
                .build();

        SessionScore lowScore = SessionScore.builder()
                .user(testUser)
                .sessionDate(testDate)
                .session(2)
                .sessionMinutes(25)
                .timestamp(1640997000.0)
                .duration(1200.0) // 20분 = 1200초
                .distractedCount(10)
                .distractedDuration(300)
                .afkDuration(180.0)
                .build();

        List<SessionScore> sessionScores = Arrays.asList(highScore, lowScore);

        when(sessionScoreRepository.findAllByUserIdAndSessionDate(userId, testDate))
                .thenReturn(sessionScores);
        when(categoryProvider.getCategoryMapById()).thenReturn(new HashMap<>());
        when(pomodoroSessionProvider.loadByUserIdAndSessionAndSessionDate(any(), any(), any()))
                .thenReturn(List.of());

        // when
        List<SessionScoreResponseDto> result = sessionScoreService.getByUserIdAndSessionDate(userId, testDate);

        // then
        assertThat(result).hasSize(2);

        // 첫 번째 세션은 높은 점수를 가져야 함 (적은 방해, 적은 AFK)
        SessionScoreResponseDto firstSessionResult = result.get(0);
        // 두 번째 세션은 낮은 점수를 가져야 함 (많은 방해, 많은 AFK)
        SessionScoreResponseDto secondSessionResult = result.get(1);

        assertThat(firstSessionResult.score()).isGreaterThan(secondSessionResult.score());
    }
}