package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategoryType;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.pomodoro.dto.DistractedDetailsDto;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.pomodoro.entity.CategorizedData;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.repository.CategorizedDataRepository;
import com.swmStrong.demo.domain.pomodoro.repository.PomodoroUsageLogRepository;
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
    private final RedisStreamProducer redisStreamProducer;
    private final ApplicationEventPublisher applicationEventPublisher;

    public PomodoroServiceImpl(
            CategorizedDataRepository categorizedDataRepository,
            PomodoroUsageLogRepository pomodoroUsageLogRepository,
            UserInfoProvider userInfoProvider,
            CategoryProvider categoryProvider,
            RedisStreamProducer redisStreamProducer,
            ApplicationEventPublisher applicationEventPublisher
    ) {
        this.categorizedDataRepository = categorizedDataRepository;
        this.pomodoroUsageLogRepository = pomodoroUsageLogRepository;
        this.userInfoProvider = userInfoProvider;
        this.categoryProvider = categoryProvider;
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

        for (PomodoroUsageLogsDto.PomodoroDto pomodoroDto: pomodoroUsageLogsDto.usageLogs()) {
            CategorizedData categorizedData = CategorizedData.builder()
                    .url(pomodoroDto.url())
                    .title(pomodoroDto.title())
                    .app(pomodoroDto.app())
                    .build();
            categorizedDataList.add(categorizedData);

            PomodoroUsageLog pomodoroUsageLog = PomodoroUsageLog.builder()
                    .session(pomodoroUsageLogsDto.session())
                    .sessionMinutes(pomodoroUsageLogsDto.sessionMinutes())
                    .sessionDate(pomodoroUsageLogsDto.sessionDate())
                    .userId(userId)
                    .duration(pomodoroDto.duration())
                    .timestamp(pomodoroDto.timestamp())
                    .build();
            pomodoroUsageLogList.add(pomodoroUsageLog);

        }

        categorizedDataList = categorizedDataRepository.saveAll(categorizedDataList);
        pomodoroUsageLogList = pomodoroUsageLogRepository.saveAll(pomodoroUsageLogList);

        for (int i=0; i<pomodoroUsageLogList.size(); i++) {
            PomodoroUsageLog pomodoroUsageLog = pomodoroUsageLogList.get(i);
            CategorizedData categorizedData = categorizedDataList.get(i);
            pomodoroUsageLog.updateCategorizedDataId(categorizedData.getId());
            boolean isEnd = pomodoroUsageLogsDto.isEnd() && i == pomodoroUsageLogList.size() - 1;
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
                    .session(pomodoroUsageLogsDto.session())
                    .duration(pomodoroUsageLog.getDuration())
                    .timestamp(pomodoroUsageLog.getTimestamp())
                    .isEnd(isEnd)
                    .build()
            );
        }
        
        pomodoroUsageLogRepository.saveAll(pomodoroUsageLogList);
        
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
        log.trace("pomodoroUsageLogList: {}", pomodoroUsageLogList);
        for (PomodoroUsageLog usageLog:  pomodoroUsageLogList) {
            log.trace("id: {}", usageLog.getCategorizedDataId());
        }
        List<PomodoroUsageLog> distractedUsageLogList = pomodoroUsageLogList.stream()
                .filter(pomodoroUsageLog -> !workCategories.contains(categoryMap.get(pomodoroUsageLog.getCategoryId())))
                .toList();
        log.trace("distractedUsageLogList: {}", distractedUsageLogList);
        Set<ObjectId> categorizedDataIds = distractedUsageLogList.stream()
                .map(PomodoroUsageLog::getCategorizedDataId)
                .collect(Collectors.toSet());
        for (ObjectId categorizedDataId : categorizedDataIds) {
            log.trace("categorizedDataId: {}", categorizedDataId);
        }
        Map<ObjectId, CategorizedData> categorizedDataMap = categorizedDataRepository.findAllById(categorizedDataIds)
                .stream()
                .collect(Collectors.toMap(CategorizedData::getId, Function.identity()));
        log.trace("categorizedDataMap: {}", categorizedDataMap);
        Map<String, Integer> distractedCountMap = new HashMap<>();
        Map<String, Double> distractedDurationMap = new HashMap<>();

        for (PomodoroUsageLog log : distractedUsageLogList) {
            CategorizedData categorizedData = categorizedDataMap.get(log.getCategorizedDataId());
            if (categorizedData != null) {
                String app = categorizedData.getApp();
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
        log.trace("distractedDetailsDtoList: {}", distractedDetailsDtoList);
        return distractedDetailsDtoList;
        // 횟수는 의미가 없다.
        // 각 App에 접근한 횟수로 인해 감소된 점수는 카운트할 수 없다.
        // 각 App에 접근한 횟수의 합은 총 distraction Block의 개수와 다르다. -> block 은 덩어리로 계산하고, 접근한 횟수는 각각의 App 로그를 합산한 결과다.
        // 하지만, 특정 App에 단순히 접근한 횟수를 서빙할 수 있다. -> 단순히 합산하면 되는 문제다. 25분동안 카톡에 30번 접근했다는 알람도 의미를 가질 수는 있다.
        // 시간은 의미가 있다.
        // 각 App에 접근한 시간으로 인해 감소된 점수는 카운트할 수 있다.
        // 각 App에 의해 까인 점수는 의미를 가지기 어렵다 -> 왜? 점수는 횟수와 시간으로 나뉜다. 하지만 횟수가 나뉘어 있는 경우 위의 이유로 구분하기 어렵다.
        // 각 App에 접근한 시간은 의미를 가질 수 있다. -> 왜? 시간은 그 자체로 의미를 가지며, 시간으로 인해 손해 본 점수를 (특정 App접속 시간/총 방해 App접속 시간 * 감소된 점수) 으로 나타낼 수 있다.

        // 세션은 정보를 들고 있지만, UsageLog들은 정보를 들고 있지 않다.
        // 그리고 이 api는 세션 정보를 가지고 있다는 가정 하에 요청할 수 있다.
        // 그리고 웹은 세션에 추가적으로 요청을 보내는 것보다, 정보를 쥐고 있는게 훨씬 낫다.

        // 그럼 여기서는 List< DistractedApp, duration, count > 만 보내야 하는가?
    }
}
