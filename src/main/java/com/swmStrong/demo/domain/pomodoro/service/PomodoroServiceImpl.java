package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
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
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class PomodoroServiceImpl implements PomodoroService {

    private final CategorizedDataRepository categorizedDataRepository;
    private final PomodoroUsageLogRepository pomodoroUsageLogRepository;
    private final UserInfoProvider userInfoProvider;
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
}
