package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.enums.Browsers;
import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategoryType;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.util.DomainExtractor;
import com.swmStrong.demo.domain.pomodoro.dto.DistractedDetailsDto;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.pomodoro.entity.CategorizedData;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.repository.CategorizedDataRepository;
import com.swmStrong.demo.domain.pomodoro.repository.PomodoroUsageLogRepository;
import com.swmStrong.demo.domain.sessionScore.facade.SessionNumberProvider;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.PomodoroPatternClassifyMessage;
import com.swmStrong.demo.message.event.UsageLogCreatedEvent;
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
    private final SessionNumberProvider sessionNumberProvider;
    private final RedisStreamProducer redisStreamProducer;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PomodoroServiceImpl(
            CategorizedDataRepository categorizedDataRepository,
            PomodoroUsageLogRepository pomodoroUsageLogRepository,
            UserInfoProvider userInfoProvider,
            CategoryProvider categoryProvider,
            SessionNumberProvider sessionNumberProvider,
            RedisStreamProducer redisStreamProducer,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.categorizedDataRepository = categorizedDataRepository;
        this.pomodoroUsageLogRepository = pomodoroUsageLogRepository;
        this.userInfoProvider = userInfoProvider;
        this.categoryProvider = categoryProvider;
        this.sessionNumberProvider = sessionNumberProvider;
        this.redisStreamProducer = redisStreamProducer;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void save(String userId, PomodoroUsageLogsDto pomodoroUsageLogsDto) {
        if (!userInfoProvider.existsUserById(userId)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        List<PomodoroUsageLog> pomodoroUsageLogList = new ArrayList<>();
        List<CategorizedData> categorizedDataList = new ArrayList<>();
        int session = sessionNumberProvider.getNextSessionByUserIDAndSessionDate(userId, pomodoroUsageLogsDto.sessionDate());

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

        categorizedDataList = categorizedDataRepository.saveAll(categorizedDataList);

        for (int i=0; i<pomodoroUsageLogList.size(); i++) {
            PomodoroUsageLog pomodoroUsageLog = pomodoroUsageLogList.get(i);
            CategorizedData categorizedData = categorizedDataList.get(i);
            pomodoroUsageLog.updateCategorizedDataId(categorizedData.getId());
        }
        
        pomodoroUsageLogList = pomodoroUsageLogRepository.saveAll(pomodoroUsageLogList);

        for (int i=0; i<pomodoroUsageLogList.size(); i++) {
            PomodoroUsageLog pomodoroUsageLog = pomodoroUsageLogList.get(i);
            CategorizedData categorizedData = categorizedDataList.get(i);
            boolean isEnd = i == pomodoroUsageLogList.size() - 1;
            redisStreamProducer.send(
                    StreamConfig.POMODORO_PATTERN_MATCH.getStreamKey(),
                    PomodoroPatternClassifyMessage.builder()
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
    public List<DistractedDetailsDto> getDetailsByUserIdAndSessionDateAndSession(String userId, LocalDate date, int session) {
        List<PomodoroUsageLog> pomodoroUsageLogList = pomodoroUsageLogRepository.findByUserIdAndSessionDateAndSession(userId, date, session);
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMapById();
        Set<String> workCategories = WorkCategoryType.getAllValues();

        List<PomodoroUsageLog> distractedUsageLogList = pomodoroUsageLogList.stream()
                .filter(pomodoroUsageLog -> !workCategories.contains(categoryMap.get(pomodoroUsageLog.getCategoryId())))
                .toList();

        Set<ObjectId> categorizedDataIds = distractedUsageLogList.stream()
                .map(PomodoroUsageLog::getCategorizedDataId)
                .collect(Collectors.toSet());

        Map<ObjectId, CategorizedData> categorizedDataMap = categorizedDataRepository.findAllById(categorizedDataIds)
                .stream()
                .collect(Collectors.toMap(CategorizedData::getId, Function.identity(), (existing, replacement) -> existing));

        Map<String, Integer> distractedCountMap = new HashMap<>();
        Map<String, Double> distractedDurationMap = new HashMap<>();

        for (PomodoroUsageLog log : distractedUsageLogList) {
            CategorizedData categorizedData = categorizedDataMap.get(log.getCategorizedDataId());
            if (categorizedData != null) {
                String app = categorizedData.getApp();
                if (Browsers.patterns.contains(app.toLowerCase())) {
                    app = DomainExtractor.extractDomain(app);
                }
                distractedCountMap.merge(app, 1, Integer::sum);
                distractedDurationMap.merge(app, log.getDuration(), Double::sum);
            }
        }
        List<DistractedDetailsDto> distractedDetailsDtoList = new ArrayList<>();
        for (String app: distractedCountMap.keySet()) {
            distractedDetailsDtoList.add(
                    DistractedDetailsDto.builder()
                            .distractedApp(app)
                            .count(distractedCountMap.get(app))
                            .duration(distractedDurationMap.get(app))
                            .build()
            );
        }

        return distractedDetailsDtoList;

    }
}
