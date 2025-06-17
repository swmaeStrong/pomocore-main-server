package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.usageLog.dto.CategoryHourlyUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import com.swmStrong.demo.domain.usageLog.repository.UsageLogRepository;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.PatternClassifyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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

    @Override
    public void saveAll(String userId, List<SaveUsageLogDto> saveUsageLogDtoList) {
        try {
            List<UsageLog> usageLogs = saveUsageLogDtoList.stream()
                    .map(saveUsageLogDto -> UsageLog.builder()
                            .userId(userId)
                            .app(saveUsageLogDto.app())
                            .title(saveUsageLogDto.title())
                            .url(saveUsageLogDto.url())
                            .duration(saveUsageLogDto.duration())
                            .timestamp(saveUsageLogDto.timestamp())
                            .build())
                    .toList();

            List<UsageLog> savedUsageLogs = usageLogRepository.saveAll(usageLogs);

            for (UsageLog usageLog : savedUsageLogs) {
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
        } catch (Exception e) {
            log.error("error occurred when save usage log, {}", e.getMessage());
            throw new ApiException(ErrorCode.USAGE_LOG_NOT_FOUND);
        }
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
