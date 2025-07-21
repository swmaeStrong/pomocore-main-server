package com.swmStrong.demo.domain.pomodoro.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategoryType;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroResponseDto;
import com.swmStrong.demo.domain.pomodoro.dto.PomodoroUsageLogsDto;
import com.swmStrong.demo.domain.pomodoro.dto.SessionResponseDto;
import com.swmStrong.demo.domain.pomodoro.entity.CategorizedData;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.repository.CategorizedDataRepository;
import com.swmStrong.demo.domain.pomodoro.repository.PomodoroUsageLogRepository;
import com.swmStrong.demo.domain.user.facade.UserInfoProvider;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.PomodoroPatternClassifyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

    @Override
    public List<PomodoroResponseDto> getPomodoroSessionResult(String userId, LocalDate date) {
        if (!userInfoProvider.existsUserById(userId)) {
            throw new ApiException(ErrorCode.USER_NOT_FOUND);
        }

        List<PomodoroResponseDto> pomodoroResponseDtoList = new ArrayList<>();
        int maxSession = pomodoroUsageLogRepository.findMaxSessionByUserIdAndSessionDate(userId, date);

        for (int i = 1; i <= maxSession; i++) {
            List<PomodoroUsageLog> pomodoroUsageLogList = pomodoroUsageLogRepository.findByUserIdAndSessionAndSessionDate(userId, i, date);
            System.out.println(pomodoroUsageLogList);
            if (pomodoroUsageLogList.isEmpty()) {
                continue;
            }
            
            List<SessionResponseDto> sessionResponseDtoList = new ArrayList<>();
            int sessionMinutes = 0;
            double workTime = 0;
            double afkTime = 0;
            double breakTime = 0;

            Set<String> workCategories = WorkCategoryType.getAllValues();
            for (PomodoroUsageLog pomodoroUsageLog : pomodoroUsageLogList) {
                sessionMinutes = pomodoroUsageLog.getSessionMinutes();
                
                String category = pomodoroUsageLog.getCategoryId() != null 
                    ? categoryProvider.getCategoryById(pomodoroUsageLog.getCategoryId())
                    : null;
                if (category != null) {
                    if (category.equals("AFK")) {
                        afkTime += pomodoroUsageLog.getDuration();
                    } else if (workCategories.contains(category)) {
                        workTime += pomodoroUsageLog.getDuration();
                    } else {
                        breakTime += pomodoroUsageLog.getDuration();
                    }
                }
                SessionResponseDto sessionResponseDto = new SessionResponseDto(
                    pomodoroUsageLog.getTimestamp(),
                    pomodoroUsageLog.getDuration(),
                    category
                );
                sessionResponseDtoList.add(sessionResponseDto);
            }
            
            PomodoroResponseDto pomodoroResponseDto = PomodoroResponseDto.builder()
                .workTime(workTime)
                .afkTime(afkTime)
                .breakTime(breakTime)
                .sessionDate(date)
                .session(i)
                .sessionMinutes(sessionMinutes)
                .usageLogs(sessionResponseDtoList)
                .build();
                
            pomodoroResponseDtoList.add(pomodoroResponseDto);
        }
        
        return pomodoroResponseDtoList;
    }

    //TODO: 각 세션을 조회하고, 그 점수를 계산해서 특정 저장소에 넣는 로직 구현

    
}
