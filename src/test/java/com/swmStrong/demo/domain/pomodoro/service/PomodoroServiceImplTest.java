package com.swmStrong.demo.domain.pomodoro.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.domain.pomodoro.dto.AppUsageDto;
import com.swmStrong.demo.domain.pomodoro.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.pomodoro.entity.CategorizedData;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.repository.CategorizedDataRepository;
import com.swmStrong.demo.domain.pomodoro.repository.PomodoroUsageLogRepository;
import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.facade.SessionScoreProvider;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import com.swmStrong.demo.domain.sessionScore.service.SessionStateManager;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.infra.LLM.LLMSummaryProvider;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PomodoroServiceImpl 테스트")
class PomodoroServiceImplTest {

    @Mock
    private CategorizedDataRepository categorizedDataRepository;

    @Mock
    private PomodoroUsageLogRepository pomodoroUsageLogRepository;

    @Mock
    private UserInfoProvider userInfoProvider;

    @Mock
    private CategoryProvider categoryProvider;

    @Mock
    private SessionScoreProvider sessionScoreProvider;

    @Mock
    private SessionScoreRepository sessionScoreRepository;

    @Mock
    private SessionStateManager sessionStateManager;

    @Mock
    private RedisStreamProducer redisStreamProducer;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private LeaderboardProvider leaderboardProvider;

    @Mock
    private LLMSummaryProvider llmSummaryProvider;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private PomodoroServiceImpl pomodoroService;

    private User testUser;
    private String userId;
    private LocalDate testDate;
    private SessionScore testSessionScore;

    @BeforeEach
    void setUp() {
        userId = "user123";
        testUser = new User(userId, "테스트사용자");
        testDate = LocalDate.of(2024, 1, 15);
        testSessionScore = SessionScore.builder()
                .user(testUser)
                .sessionDate(testDate)
                .session(1)
                .sessionMinutes(25)
                .build();
    }

    @Test
    @DisplayName("뽀모도로 사용 로그 저장")
    void shouldSavePomodoroUsageLogs() {
        // given
        PomodoroUsageLogsDto.PomodoroDto pomodoroDto1 = new PomodoroUsageLogsDto.PomodoroDto(
                "GitHub",
                "Chrome",
                "https://github.com",
                300.0,
                1640995200.0
        );

        PomodoroUsageLogsDto.PomodoroDto pomodoroDto2 = new PomodoroUsageLogsDto.PomodoroDto(
                "IntelliJ IDEA",
                "IntelliJ IDEA",
                null,
                600.0,
                1640995500.0
        );

        PomodoroUsageLogsDto logsDto = new PomodoroUsageLogsDto(
                testDate,
                25,
                Arrays.asList(pomodoroDto1, pomodoroDto2)
        );

        when(userInfoProvider.loadByUserId(userId)).thenReturn(testUser);
        when(sessionScoreProvider.createSession(testUser, testDate, 25)).thenReturn(1);
        when(llmSummaryProvider.getResult(any())).thenReturn("{\"summaryKor\":\"한국어 요약\",\"summaryEng\":\"English Summary\"}");
        when(sessionScoreRepository.findByUserIdAndSessionAndSessionDate(userId, 1, testDate))
                .thenReturn(testSessionScore);
        when(categorizedDataRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(pomodoroUsageLogRepository.saveAll(anyList()))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // when
        pomodoroService.save(userId, logsDto);

        // then
        verify(sessionStateManager).initializeSessionProcessing(userId, testDate, 1);
        verify(leaderboardProvider).increaseSessionCount(userId, testDate);
        verify(categorizedDataRepository).saveAll(anyList());
        verify(pomodoroUsageLogRepository).saveAll(anyList());
        verify(sessionScoreRepository).save(testSessionScore);
        verify(applicationEventPublisher).publishEvent(any());
        verify(redisStreamProducer).sendBatch(any(), anyList());
    }

    @Test
    @DisplayName("날짜별 사용량 조회")
    void shouldGetUsageLogByUserIdAndDateBetween() {
        // given
        List<CategoryUsageDto> expectedUsages = Arrays.asList(
                CategoryUsageDto.builder()
                        .category("개발")
                        .duration(1500.0)
                        .build(),
                CategoryUsageDto.builder()
                        .category("학습")
                        .duration(1200.0)
                        .build()
        );

        when(pomodoroUsageLogRepository.findByUserIdAndTimestampBetween(
                eq(userId), anyDouble(), anyDouble()))
                .thenReturn(expectedUsages);

        // when
        List<CategoryUsageDto> result = pomodoroService.getUsageLogByUserIdAndDateBetween(userId, testDate);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).category()).isEqualTo("개발");
        assertThat(result.get(0).duration()).isEqualTo(1500.0);
        assertThat(result.get(1).category()).isEqualTo("학습");
        assertThat(result.get(1).duration()).isEqualTo(1200.0);
    }

    @Test
    @DisplayName("주간 상세 정보 조회")
    void shouldGetWeeklyDetailsByUserIdAndSessionDate() {
        // given
        LocalDate weekDate = LocalDate.of(2024, 1, 10); // 수요일
        ObjectId categoryId = new ObjectId();
        ObjectId dataId = new ObjectId();

        List<PomodoroUsageLog> logs = Arrays.asList(
                PomodoroUsageLog.builder()
                        .userId(userId)
                        .categoryId(categoryId)
                        .CategorizedDataId(dataId)
                        .duration(1500.0)
                        .sessionDate(LocalDate.of(2024, 1, 8))
                        .build(),
                PomodoroUsageLog.builder()
                        .userId(userId)
                        .categoryId(categoryId)
                        .CategorizedDataId(dataId)
                        .duration(1200.0)
                        .sessionDate(LocalDate.of(2024, 1, 10))
                        .build()
        );

        CategorizedData categorizedData = CategorizedData.builder()
                .app("Chrome")
                .url("https://github.com")
                .title("GitHub")
                .build();

        when(pomodoroUsageLogRepository.findByUserIdAndSessionDateBetween(userId, any(), any()))
                .thenReturn(logs);
        when(categoryProvider.getCategoryMapById()).thenReturn(java.util.Map.of(categoryId, "개발"));
        when(categorizedDataRepository.findAllById(any())).thenReturn(List.of(categorizedData));

        // when
        AppUsageDto result = pomodoroService.getWeeklyDetailsByUserIdAndSessionDate(userId, weekDate);

        // then
        assertThat(result.totalSeconds()).isEqualTo(2700.0);
        assertThat(result.dailyResults()).isNotNull();
        assertThat(result.categoryUsages()).isNotNull();
    }

    @Test
    @DisplayName("사용자별 세션별 상세 정보 조회")
    void shouldGetDetailsByUserIdAndSessionDateAndSession() {
        // given
        ObjectId categoryId = new ObjectId();
        ObjectId dataId1 = new ObjectId();
        ObjectId dataId2 = new ObjectId();

        CategorizedData data1 = CategorizedData.builder()
                .app("Chrome")
                .url("https://github.com")
                .title("GitHub")
                .build();
        CategorizedData data2 = CategorizedData.builder()
                .app("IntelliJ IDEA")
                .url(null)
                .title("IntelliJ IDEA")
                .build();

        List<PomodoroUsageLog> logs = Arrays.asList(
                PomodoroUsageLog.builder()
                        .userId(userId)
                        .categoryId(categoryId)
                        .CategorizedDataId(dataId1)
                        .duration(1500.0)
                        .sessionDate(testDate)
                        .build(),
                PomodoroUsageLog.builder()
                        .userId(userId)
                        .categoryId(categoryId)
                        .CategorizedDataId(dataId2)
                        .duration(1200.0)
                        .sessionDate(testDate)
                        .build()
        );

        when(pomodoroUsageLogRepository.findByUserIdAndSessionDate(userId, testDate))
                .thenReturn(logs);
        when(categoryProvider.getCategoryMapById())
                .thenReturn(java.util.Map.of(categoryId, "개발"));
        when(categorizedDataRepository.findAllById(any()))
                .thenReturn(List.of(data1, data2));

        // when
        AppUsageDto result = pomodoroService.getDetailsByUserIdAndSessionDateAndSession(userId, testDate, null);

        // then
        assertThat(result.totalSeconds()).isEqualTo(2700.0);
        assertThat(result.workAppUsage()).isNotEmpty();
    }


    @Test
    @DisplayName("빈 사용 로그 처리")
    void shouldHandleEmptyUsageLogs() {
        // given
        when(pomodoroUsageLogRepository.findByUserIdAndSessionDate(userId, testDate))
                .thenReturn(List.of());
        when(categoryProvider.getCategoryMapById()).thenReturn(java.util.Map.of());

        // when
        AppUsageDto result = pomodoroService.getDetailsByUserIdAndSessionDateAndSession(userId, testDate, null);

        // then
        assertThat(result.totalSeconds()).isEqualTo(0.0);
        assertThat(result.workAppUsage()).isEmpty();
        assertThat(result.distractedAppUsage()).isEmpty();
    }
}