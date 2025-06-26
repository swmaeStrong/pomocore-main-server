package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.usageLog.dto.CategoryHourlyUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import com.swmStrong.demo.domain.usageLog.repository.UsageLogRepository;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.PatternClassifyMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class UsageLogServiceImpl implements UsageLogService {

    private static final String USAGE_LOG_LAST_TIMESTAMP_PREFIX = "usageLog:lastTimestamp:";
    private static final long CACHE_EXPIRE_SECONDS = 86400; // 24 hours
    private static final double TIME_TOLERANCE_SECONDS = 60.0;

    private final UsageLogRepository usageLogRepository;
    private final RedisStreamProducer redisStreamProducer;
    private final RedisRepository redisRepository;

    public UsageLogServiceImpl(
            UsageLogRepository usageLogRepository,
            RedisStreamProducer redisStreamProducer,
            RedisRepository redisRepository
    ) {
        this.usageLogRepository = usageLogRepository;
        this.redisStreamProducer = redisStreamProducer;
        this.redisRepository = redisRepository;
    }
    //TODO: 가입 메일 보내기.
    //TODO: 메일 서버 만들기.
    //TODO: 서버 시간 내려주기?
    @Override
    public void saveAll(String userId, List<SaveUsageLogDto> saveUsageLogDtoList) {

        double currentTimestamp = Instant.now().getEpochSecond();
        log.trace("current timestamp: {} ({})",  currentTimestamp, LocalDateTime.now());
        String cacheKey = USAGE_LOG_LAST_TIMESTAMP_PREFIX + userId;
        try {
            String lastTimestampStr = redisRepository.getData(cacheKey);
            Double lastTimestamp = lastTimestampStr != null ? Double.parseDouble(lastTimestampStr) : null;

            for (SaveUsageLogDto dto : saveUsageLogDtoList) {
                if (dto.timestamp() > currentTimestamp) {
                    log.warn("Invalid future timestamp {} for user {}", dto.timestamp(), userId);
                    throw new ApiException(ErrorCode.REQUEST_TIME_IS_FUTURE);
                }

                if (lastTimestamp != null && dto.timestamp() < lastTimestamp) {
                    log.warn("Invalid timestamp {} before last timestamp {} for user {}",
                            dto.timestamp(), lastTimestamp, userId);
                    throw new ApiException(ErrorCode.REQUEST_TIME_CONTAIN_BEFORE_SAVED);
                }

                double endTimestamp = dto.timestamp() + dto.duration() - TIME_TOLERANCE_SECONDS;
                if (endTimestamp > currentTimestamp) {
                    log.warn("Invalid log with end time {} in the future for user {}",
                            endTimestamp, userId);
                    throw new ApiException(ErrorCode.REQUEST_TIME_IS_OVER_FUTURE);
                }
            }

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

            savedUsageLogs.stream()
                    .map(UsageLog::getTimestamp)
                    .max(Double::compareTo)
                    .ifPresent(maxTimestamp -> 
                        redisRepository.setDataWithExpire(cacheKey, maxTimestamp.toString(), CACHE_EXPIRE_SECONDS)
                    );

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
        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("error occurred when save usage log, {}", e.getMessage());
            throw new ApiException(ErrorCode.LOG_SAVE_FAILED);
        }
    }

    @Override
    public List<UsageLogResponseDto> getUsageLogByUserId(String userId) {
        List<UsageLog> usageLogs = usageLogRepository.findByUserId(userId);

        return usageLogs.stream().map(UsageLogResponseDto::from).toList();
    }

    @Override
    public List<CategoryUsageDto> getUsageLogByUserIdAndDate(String userId, LocalDate date) {
        DateRange range = getDateRange(date);
        return usageLogRepository.findByUserIdAndTimestampBetween(userId, range.start(), range.end());
    }

    @Override
    public List<CategoryHourlyUsageDto> getUsageLogByUserIdAndDateHourly(String userId, LocalDate date, Integer binSize) {
        DateRange range = getDateRange(date);
        return usageLogRepository.findHourlyCategoryUsageByUserIdAndTimestampBetween(userId, range.start(), range.end(), binSize);
    }
    
    private DateRange getDateRange(LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }
        
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        
        double startTimestamp = start.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        double endTimestamp = end.atZone(java.time.ZoneId.systemDefault()).toEpochSecond();
        
        return new DateRange(startTimestamp, endTimestamp);
    }
    
    private record DateRange(double start, double end) {}
}
