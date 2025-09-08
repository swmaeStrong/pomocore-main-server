package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.domain.categoryPattern.enums.Browsers;
import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategory;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.util.DomainExtractor;
import com.swmStrong.demo.domain.leaderboard.facade.LeaderboardProvider;
import com.swmStrong.demo.domain.pomodoro.dto.AppUsageDto;
import com.swmStrong.demo.domain.pomodoro.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.pomodoro.dto.AppUsageResult;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.pomodoro.entity.CategorizedData;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.repository.CategorizedDataRepository;
import com.swmStrong.demo.domain.pomodoro.repository.PomodoroUsageLogRepository;
import com.swmStrong.demo.domain.sessionScore.entity.SessionScore;
import com.swmStrong.demo.domain.sessionScore.facade.SessionScoreProvider;
import com.swmStrong.demo.domain.sessionScore.repository.SessionScoreRepository;
import com.swmStrong.demo.domain.sessionScore.service.SessionStateManager;
import com.swmStrong.demo.infra.LLM.LLMSummaryProvider;
import com.swmStrong.demo.domain.user.entity.User;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.PomodoroPatternClassifyMessage;
import com.swmStrong.demo.message.event.UsageLogCreatedEvent;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PomodoroServiceImpl implements PomodoroService {

    private final CategorizedDataRepository categorizedDataRepository;
    private final PomodoroUsageLogRepository pomodoroUsageLogRepository;
    private final UserInfoProvider userInfoProvider;
    private final CategoryProvider categoryProvider;
    private final SessionScoreProvider sessionScoreProvider;
    private final SessionScoreRepository sessionScoreRepository;
    private final SessionStateManager sessionStateManager;
    private final RedisStreamProducer redisStreamProducer;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LeaderboardProvider leaderboardProvider;
    private final LLMSummaryProvider llmSummaryProvider;

    public PomodoroServiceImpl(
            CategorizedDataRepository categorizedDataRepository,
            PomodoroUsageLogRepository pomodoroUsageLogRepository,
            UserInfoProvider userInfoProvider,
            CategoryProvider categoryProvider,
            SessionScoreProvider sessionScoreProvider,
            SessionScoreRepository sessionScoreRepository,
            SessionStateManager sessionStateManager,
            RedisStreamProducer redisStreamProducer,
            ApplicationEventPublisher applicationEventPublisher,
            LeaderboardProvider leaderboardProvider,
            LLMSummaryProvider llmSummaryProvider
    ) {
        this.categorizedDataRepository = categorizedDataRepository;
        this.pomodoroUsageLogRepository = pomodoroUsageLogRepository;
        this.userInfoProvider = userInfoProvider;
        this.categoryProvider = categoryProvider;
        this.sessionScoreProvider = sessionScoreProvider;
        this.sessionScoreRepository = sessionScoreRepository;
        this.sessionStateManager = sessionStateManager;
        this.redisStreamProducer = redisStreamProducer;
        this.applicationEventPublisher = applicationEventPublisher;
        this.leaderboardProvider = leaderboardProvider;
        this.llmSummaryProvider = llmSummaryProvider;
    }

    @Transactional
    @Override
    public void save(String userId, PomodoroUsageLogsDto pomodoroUsageLogsDto) {
        User user = userInfoProvider.loadByUserId(userId);

        List<PomodoroUsageLog> pomodoroUsageLogList = new ArrayList<>();
        List<CategorizedData> categorizedDataList = new ArrayList<>();

        int session = sessionScoreProvider.createSession(user, pomodoroUsageLogsDto.sessionDate(), pomodoroUsageLogsDto.sessionMinutes());

        sessionStateManager.initializeSessionProcessing(userId, pomodoroUsageLogsDto.sessionDate(), session);

        leaderboardProvider.increaseSessionCount(userId, pomodoroUsageLogsDto.sessionDate());

        for (PomodoroUsageLogsDto.PomodoroDto pomodoroDto: pomodoroUsageLogsDto.usageLogs()) {
            CategorizedData categorizedData = CategorizedData.builder()
                    .url(pomodoroDto.url())
                    .title(pomodoroDto.title())
                    .app(pomodoroDto.app())
                    .build();
            categorizedDataList.add(categorizedData);

            PomodoroUsageLog pomodoroUsageLog = PomodoroUsageLog.builder()
                    .session(session)
                    .sessionMinutes(pomodoroUsageLogsDto.sessionMinutes())
                    .sessionDate(pomodoroUsageLogsDto.sessionDate())
                    .userId(userId)
                    .duration(pomodoroDto.duration())
                    .timestamp(pomodoroDto.timestamp())
                    .build();
            pomodoroUsageLogList.add(pomodoroUsageLog);
        }
        //TODO: 이후 낙관적 락을 통한 동시성 제어 필요.
        String sessionData = getString(categorizedDataList, pomodoroUsageLogList);

        String summary = llmSummaryProvider.getResult(sessionData);

        SessionScore sessionScore = sessionScoreRepository.findByUserIdAndSessionAndSessionDate(
                userId, session, pomodoroUsageLogsDto.sessionDate());
        if (sessionScore != null) {
            sessionScore.updateTitle(summary);
            sessionScoreRepository.save(sessionScore);
        }

        categorizedDataList = categorizedDataRepository.saveAll(categorizedDataList);

        for (int i=0; i<pomodoroUsageLogList.size(); i++) {
            PomodoroUsageLog pomodoroUsageLog = pomodoroUsageLogList.get(i);
            CategorizedData categorizedData = categorizedDataList.get(i);
            pomodoroUsageLog.updateCategorizedDataId(categorizedData.getId());
        }

        pomodoroUsageLogList = pomodoroUsageLogRepository.saveAll(pomodoroUsageLogList);

        List<PomodoroPatternClassifyMessage> messages = new ArrayList<>();
        for (int i=0; i<pomodoroUsageLogList.size(); i++) {
            PomodoroUsageLog pomodoroUsageLog = pomodoroUsageLogList.get(i);
            CategorizedData categorizedData = categorizedDataList.get(i);
            boolean isEnd = i == pomodoroUsageLogList.size() - 1;
            messages.add(PomodoroPatternClassifyMessage.builder()
                    .userId(userId)
                    .sessionMinutes(pomodoroUsageLogsDto.sessionMinutes())
                    .pomodoroUsageLogId(pomodoroUsageLog.getId().toHexString())
                    .categorizedDataId(categorizedData.getId().toHexString())
                    .title(categorizedData.getTitle())
                    .url(categorizedData.getUrl())
                    .app(categorizedData.getApp())
                    .sessionDate(pomodoroUsageLogsDto.sessionDate())
                    .session(session)
                    .duration(pomodoroUsageLog.getDuration())
                    .timestamp(pomodoroUsageLog.getTimestamp())
                    .isEnd(isEnd)
                    .build()
            );
        }

        redisStreamProducer.sendBatch(StreamConfig.POMODORO_PATTERN_MATCH.getStreamKey(), messages);

        applicationEventPublisher.publishEvent(UsageLogCreatedEvent.builder()
                .userId(userId)
                .activityDate(pomodoroUsageLogsDto.sessionDate())
                .build()
        );
    }


    @Override
    public List<CategoryUsageDto> getUsageLogByUserIdAndDateBetween(String userId, LocalDate date) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);

        double startTimestamp = start.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        double endTimestamp = end.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();

        return pomodoroUsageLogRepository.findByUserIdAndTimestampBetween(userId, startTimestamp, endTimestamp);
    }

    @Override
    public AppUsageDto getDetailsByUserIdAndSessionDateAndSession(String userId, LocalDate date, Integer session) {
        List<PomodoroUsageLog> pomodoroUsageLogList;
        if (session == null) {
            pomodoroUsageLogList = pomodoroUsageLogRepository.findByUserIdAndSessionDate(userId, date);
        } else {
            pomodoroUsageLogList = pomodoroUsageLogRepository.findByUserIdAndSessionDateAndSession(userId, date, session);
        }
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategory.categories;

        List<PomodoroUsageLog> distractedUsageLogList = new ArrayList<>();
        List<PomodoroUsageLog> workUsageLogList = new ArrayList<>();

        for (PomodoroUsageLog pomodoroUsageLog : pomodoroUsageLogList) {
            if (workCategories.contains(categoryMap.get(pomodoroUsageLog.getCategoryId()))) {
                workUsageLogList.add(pomodoroUsageLog);
            } else {
                distractedUsageLogList.add(pomodoroUsageLog);
            }
        }

        Set<ObjectId> allCategorizedDataIds = new HashSet<>();
        for (PomodoroUsageLog log : pomodoroUsageLogList) {
            allCategorizedDataIds.add(log.getCategorizedDataId());
        }

        Map<ObjectId, CategorizedData> categorizedDataMap = categorizedDataRepository.findAllById(allCategorizedDataIds)
                .stream()
                .collect(Collectors.toMap(CategorizedData::getId, Function.identity(), (existing, replacement) -> existing));

        Map<String, Integer> distractedCountMap = new HashMap<>();
        Map<String, Double> distractedDurationMap = new HashMap<>();
        Map<String, Integer> workCountMap = new HashMap<>();
        Map<String, Double> workDurationMap = new HashMap<>();

        for (PomodoroUsageLog log : distractedUsageLogList) {
            CategorizedData categorizedData = categorizedDataMap.get(log.getCategorizedDataId());
            if (categorizedData != null) {
                String app = categorizedData.getApp();
                if (Browsers.patterns.contains(app.toLowerCase())) {
                    String domainUrl = DomainExtractor.extractDomain(categorizedData.getUrl());
                    if (domainUrl != null && !domainUrl.isEmpty()) {
                        app = DomainExtractor.extractDomain(categorizedData.getUrl());
                    }
                }
                distractedCountMap.merge(app, 1, Integer::sum);
                distractedDurationMap.merge(app, log.getDuration(), Double::sum);
            }
        }

        for (PomodoroUsageLog log : workUsageLogList) {
            CategorizedData categorizedData = categorizedDataMap.get(log.getCategorizedDataId());
            if (categorizedData != null) {
                String app = categorizedData.getApp();
                if (Browsers.patterns.contains(app.toLowerCase())) {
                    String domainUrl = DomainExtractor.extractDomain(categorizedData.getUrl());
                    if (domainUrl != null && !domainUrl.isEmpty()) {
                        app = DomainExtractor.extractDomain(categorizedData.getUrl());
                    }
                }
                workCountMap.merge(app, 1, Integer::sum);
                workDurationMap.merge(app, log.getDuration(), Double::sum);
            }
        }

        List<AppUsageResult> distractedAppUsageList = new ArrayList<>();
        for (String app: distractedCountMap.keySet()) {
            distractedAppUsageList.add(
                    AppUsageResult.builder()
                            .app(app)
                            .count(distractedCountMap.get(app))
                            .duration(distractedDurationMap.get(app))
                            .build()
            );
        }

        List<AppUsageResult> workAppUsageList = new ArrayList<>();
        for (String app: workCountMap.keySet()) {
            workAppUsageList.add(
                    AppUsageResult.builder()
                            .app(app)
                            .count(workCountMap.get(app))
                            .duration(workDurationMap.get(app))
                            .build()
            );
        }

        return AppUsageDto.from(distractedAppUsageList, workAppUsageList);
    }

    private static String getString(List<CategorizedData> categorizedDataList, List<PomodoroUsageLog> pomodoroUsageLogList) {
        StringBuilder sessionDataBuilder = new StringBuilder();
        for (int i = 0; i < categorizedDataList.size(); i++) {
            CategorizedData data = categorizedDataList.get(i);
            PomodoroUsageLog log = pomodoroUsageLogList.get(i);
            sessionDataBuilder.append(String.format(
                "App: %s, Title: %s, URL: %s, Duration: %d초",
                data.getApp() != null ? data.getApp() : "Unknown",
                data.getTitle() != null ? data.getTitle() : "No title",
                data.getUrl() != null ? data.getUrl() : "No URL",
                (int) log.getDuration()
            ));
            if (i < categorizedDataList.size() - 1) {
                sessionDataBuilder.append("\n");
            }
        }
        return sessionDataBuilder.toString();
    }
}
