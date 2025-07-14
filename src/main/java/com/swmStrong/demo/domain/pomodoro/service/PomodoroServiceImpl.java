package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.pomodoro.entity.CategorizedData;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.repository.CategorizedDataRepository;
import com.swmStrong.demo.domain.pomodoro.repository.PomodoroUsageLogRepository;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.PomodoroPatternClassifyMessage;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class PomodoroServiceImpl implements PomodoroService {

    private final CategorizedDataRepository categorizedDataRepository;
    private final PomodoroUsageLogRepository pomodoroUsageLogRepository;
    private final UserInfoProvider userInfoProvider;
    private final CategoryProvider categoryProvider;
    private final RedisStreamProducer redisStreamProducer;

    public PomodoroServiceImpl(
            CategorizedDataRepository categorizedDataRepository,
            PomodoroUsageLogRepository pomodoroUsageLogRepository,
            UserInfoProvider userInfoProvider,
            CategoryProvider categoryProvider,
            RedisStreamProducer redisStreamProducer
    ) {
        this.categorizedDataRepository = categorizedDataRepository;
        this.pomodoroUsageLogRepository = pomodoroUsageLogRepository;
        this.userInfoProvider = userInfoProvider;
        this.categoryProvider = categoryProvider;
        this.redisStreamProducer = redisStreamProducer;
    }

    @Override
    public void save(String userId, PomodoroUsageLogsDto pomodoroUsageLogsDto) {
        if (!userInfoProvider.existsUserById(userId)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        Map<String, ObjectId> categoryMap = categoryProvider.getCategoryMapByCategory();
        List<PomodoroUsageLog> pomodoroUsageLogList = new ArrayList<>();
        List<CategorizedData> categorizedDataList = new ArrayList<>();

        for (PomodoroUsageLogsDto.PomodoroDto pomodoroDto: pomodoroUsageLogsDto.usageLogs()) {
            PomodoroUsageLog pomodoroUsageLog = PomodoroUsageLog.builder()
                    .session(pomodoroUsageLogsDto.session())
                    .sessionMinutes(pomodoroUsageLogsDto.sessionMinutes())
                    .sessionDate(pomodoroUsageLogsDto.sessionDate())
                    .userId(userId)
                    .categoryId(categoryMap.getOrDefault(pomodoroDto.category(), null))
                    .app(pomodoroDto.app())
                    .duration(pomodoroDto.duration())
                    .timestamp(pomodoroDto.timestamp())
                    .build();
            pomodoroUsageLogList.add(pomodoroUsageLog);

            CategorizedData categorizedData = CategorizedData.builder()
                    .url(pomodoroDto.url())
                    .title(pomodoroDto.title())
                    .app(pomodoroDto.app())
                    .categoryId(categoryMap.getOrDefault(pomodoroDto.category(), null))
                    .build();
            categorizedDataList.add(categorizedData);
        }

        pomodoroUsageLogList = pomodoroUsageLogRepository.saveAll(pomodoroUsageLogList);
        categorizedDataList = categorizedDataRepository.saveAll(categorizedDataList);

        for (int i=0; i<pomodoroUsageLogList.size(); i++) {
            PomodoroUsageLog pomodoroUsageLog = pomodoroUsageLogList.get(i);
            CategorizedData categorizedData = categorizedDataList.get(i);
            if (pomodoroUsageLog.getCategoryId() != null) continue;
            redisStreamProducer.send(
                    StreamConfig.POMODORO_PATTERN_MATCH.getStreamKey(),
                    PomodoroPatternClassifyMessage.builder()
                    .pomodoroUsageLogId(pomodoroUsageLog.getId().toHexString())
                    .categorizedDataId(categorizedData.getId().toHexString())
                    .title(categorizedData.getTitle())
                    .url(categorizedData.getUrl())
                    .app(categorizedData.getApp())
                    .build()
            );
        }
    }
}
