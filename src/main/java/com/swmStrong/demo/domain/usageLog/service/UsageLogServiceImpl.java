package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.util.TimeZoneUtil;
import com.swmStrong.demo.domain.usageLog.dto.*;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import com.swmStrong.demo.domain.usageLog.repository.UsageLogRepository;
import com.swmStrong.demo.infra.redis.repository.RedisRepository;
import com.swmStrong.demo.infra.redis.stream.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.stream.StreamConfig;
import com.swmStrong.demo.message.dto.PatternClassifyMessage;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class UsageLogServiceImpl implements UsageLogService {

    private static final String USAGE_LOG_LAST_TIMESTAMP_PREFIX = "usageLog:lastTimestamp:";
    private static final long CACHE_EXPIRE_SECONDS = 86400; // 24 hours
    private static final double TIME_TOLERANCE_SECONDS = 60.0;

    private final UsageLogRepository usageLogRepository;
    private final RedisStreamProducer redisStreamProducer;
    private final RedisRepository redisRepository;
    private final CategoryProvider categoryProvider;

    public UsageLogServiceImpl(
            UsageLogRepository usageLogRepository,
            RedisStreamProducer redisStreamProducer,
            RedisRepository redisRepository,
            CategoryProvider categoryProvider
    ) {
        this.usageLogRepository = usageLogRepository;
        this.redisStreamProducer = redisStreamProducer;
        this.redisRepository = redisRepository;
        this.categoryProvider = categoryProvider;
    }
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
    public List<CategorizedUsageLogDto> getCategorizedUsageLogByUserId(String userId) {
        List<CategorizedUsageLogDto> categorizedUsageLogDtos = new ArrayList<>();
        List<UsageLog> usageLogs = usageLogRepository.findByUserId(userId);
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMap();

        CategorizedUsageLogDto lastUsage = null;
        for (UsageLog usageLog : usageLogs) {
            String category = categoryMap.get(usageLog.getCategoryId());
            
            if (lastUsage == null || 
                !lastUsage.title().equals(usageLog.getTitle()) ||
                !lastUsage.app().equals(usageLog.getApp()) ||
                !lastUsage.category().equals(category)) {
                
                CategorizedUsageLogDto currentUsage = CategorizedUsageLogDto.builder()
                        .app(usageLog.getApp())
                        .category(category)
                        .title(usageLog.getTitle())
                        .timestamp(TimeZoneUtil.convertUnixToLocalDateTime(usageLog.getTimestamp(), TimeZoneUtil.KOREA_TIMEZONE))
                        .build();
                
                categorizedUsageLogDtos.add(currentUsage);
                lastUsage = currentUsage;
            }
        }
        Collections.reverse(categorizedUsageLogDtos);
        return categorizedUsageLogDtos;
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
