package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.domain.usageLog.dto.CategoryHourlyUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import com.swmStrong.demo.domain.usageLog.repository.UsageLogRepository;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.PatternClassifyMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsageLogServiceImpl implements UsageLogService {

    private final UsageLogRepository usageLogRepository;
    private final RedisStreamProducer redisStreamProducer;

    public UsageLogServiceImpl(
            UsageLogRepository usageLogRepository,
            RedisStreamProducer redisStreamProducer
    ) {
        this.usageLogRepository = usageLogRepository;
        this.redisStreamProducer = redisStreamProducer;
    }

    //TODO: 문제가 발생할 가능성도 있으니 try - except 구조로 변경
    @Override
    public void saveAll(String userId, List<SaveUsageLogDto> saveUsageLogDtoList) {
        for (SaveUsageLogDto saveUsageLogDto : saveUsageLogDtoList) {
            save(userId, saveUsageLogDto);
        }
    }


    private void save(String userId, SaveUsageLogDto saveUsageLogDto) {
        UsageLog usageLog = usageLogRepository.save(
                UsageLog.builder()
                .userId(userId)
                .app(saveUsageLogDto.app())
                .title(saveUsageLogDto.title())
                .url(saveUsageLogDto.url())
                .duration(saveUsageLogDto.duration())
                .timestamp(saveUsageLogDto.timestamp())
                .build()
        );

        redisStreamProducer.send(
                StreamConfig.PATTERN_MATCH.getStreamKey(),
                PatternClassifyMessage.builder()
                        .usageLogId(usageLog.getId().toHexString())
                        .app(usageLog.getApp())
                        .title(usageLog.getTitle())
                        .url(usageLog.getUrl())
                        .build()
        );
    }

    @Override
    public List<UsageLogResponseDto> getUsageLogByUserId(String userId) {
        List<UsageLog> usageLogs = usageLogRepository.findByUserId(userId);

        return usageLogs.stream().map(UsageLogResponseDto::from).toList();
    }

    @Override
    public List<CategoryUsageDto> getUsageLogByUserIdAndDate(String userId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return usageLogRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }

    @Override
    public List<CategoryHourlyUsageDto> getUsageLogByUserIdAndDateHourly(String userId, LocalDate date, Integer binSize) {
        if (date == null) {
            date = LocalDate.now();
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return usageLogRepository.findHourlyCategoryUsageByUserIdAndTimestampBetween(userId, start, end, binSize);
    }
}
