package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.categoryPattern.enums.WorkCategoryType;
import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.common.util.EncryptionUtil;
import com.swmStrong.demo.domain.common.util.TimeZoneUtil;
import com.swmStrong.demo.domain.usageLog.dto.*;
import com.swmStrong.demo.domain.usageLog.dto.MergedCategoryUsageLogDto;
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
import java.util.*;

@Slf4j
@Service
public class UsageLogServiceImpl implements UsageLogService {

    private static final String USAGE_LOG_LAST_TIMESTAMP_PREFIX = "usageLog:lastTimestamp:";
    private static final long CACHE_EXPIRE_SECONDS = 86400; // 24 hours
    private static final double TIME_TOLERANCE_SECONDS = 60.0;
    private static final double CONTINUITY_MARGIN_SECONDS = 0.5;
    private static final int GAP_THRESHOLD = 1;

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

    @Override
    public void saveAll(String userId, List<SaveUsageLogDto> saveUsageLogs) {
        if (saveUsageLogs.isEmpty()) return;

        double currentTimestamp = Instant.now().getEpochSecond();
        log.trace("current timestamp: {} ({})",  currentTimestamp, LocalDateTime.now());

        String lastTimeCacheKey = USAGE_LOG_LAST_TIMESTAMP_PREFIX + userId;
        try {
            Double lastTimestamp = null;
            String lastTimeCacheValue = redisRepository.getData(lastTimeCacheKey);
            if (lastTimeCacheValue != null) {
                lastTimestamp = Double.parseDouble(lastTimeCacheValue);
            }

            ObjectId lastUsageLogId = usageLogRepository.findTopByUserIdOrderByTimestampDesc(userId)
                    .map(UsageLog::getId)
                    .orElse(null);
            List<SaveUsageLogDto>mergedUsageLogs = mergeDuplicatedData(saveUsageLogs, lastUsageLogId);

            double maxTimestamp = 0;
            for (SaveUsageLogDto dto : mergedUsageLogs) {
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

                maxTimestamp = Math.max(maxTimestamp, endTimestamp);
            }

            List<UsageLog> existingUsageLogs = new ArrayList<>();
            List<UsageLog> newUsageLogs = new ArrayList<>();

            for (SaveUsageLogDto saveUsageLogDto : mergedUsageLogs) {
                if (saveUsageLogDto.usageLogId() != null) {
                    UsageLog existingUsageLog = usageLogRepository.findById(saveUsageLogDto.usageLogId())
                            .orElseThrow(() -> new ApiException(ErrorCode.USAGE_LOG_NOT_FOUND));

                    UsageLog updatedUsageLog = existingUsageLog.updateDuration(
                            existingUsageLog.getDuration() + saveUsageLogDto.duration()
                    );
                    existingUsageLogs.add(updatedUsageLog);
                } else {
                    UsageLog newUsageLog = UsageLog.builder()
                            .userId(userId)
                            .app(saveUsageLogDto.app())
                            .title(saveUsageLogDto.title())
                            .url(saveUsageLogDto.url())
                            .duration(saveUsageLogDto.duration())
                            .timestamp(saveUsageLogDto.timestamp())
                            .build();
                    newUsageLogs.add(newUsageLog);
                }
            }


            if (!existingUsageLogs.isEmpty()) {
                usageLogRepository.saveAll(existingUsageLogs);

                for (UsageLog existingUsageLog : existingUsageLogs) {
                    redisStreamProducer.send(
                            StreamConfig.PATTERN_MATCH.getStreamKey(),
                            PatternClassifyMessage.builder()
                                    .usageLogId(existingUsageLog.getId().toHexString())
                                    .categoryId(existingUsageLog.getCategoryId().toHexString())
                                    .app(null)
                                    .title(null)
                                    .url(null)
                                    .build()
                    );
                }
            }

            if (!newUsageLogs.isEmpty()) {
                List<UsageLog> securedUsageLogs = newUsageLogs.stream()
                        .map(this::toSecuredEntity)
                        .toList();

                usageLogRepository.saveAll(securedUsageLogs);

                for (int i = 0; i < securedUsageLogs.size(); i++) {
                    UsageLog securedUsageLog = securedUsageLogs.get(i);
                    UsageLog originalUsageLog = newUsageLogs.get(i);

                    redisStreamProducer.send(
                            StreamConfig.PATTERN_MATCH.getStreamKey(),
                            PatternClassifyMessage.builder()
                                    .usageLogId(securedUsageLog.getId().toHexString())
                                    .app(originalUsageLog.getApp())
                                    .title(originalUsageLog.getTitle())
                                    .url(originalUsageLog.getUrl())
                                    .categoryId(null)
                                    .build()
                    );
                }
            }

            redisRepository.setDataWithExpire(lastTimeCacheKey, maxTimestamp + "", CACHE_EXPIRE_SECONDS);

        } catch (ApiException e) {
            throw e;
        } catch (Exception e) {
            log.error("error occurred when save usage log, {}", e);
            throw new ApiException(ErrorCode.LOG_SAVE_FAILED);
        }
    }

    @Override
    public List<CategorizedUsageLogDto> getCategorizedUsageLogByUserId(String userId, LocalDate date) {
        List<CategorizedUsageLogDto> categorizedUsageLogDtos = new ArrayList<>();
        DateRange range = getDateRange(date);
        List<UsageLog> usageLogs = usageLogRepository.findUsageLogByUserIdAndTimestampBetween(userId, range.start(), range.end());
        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMap();
        CategorizedUsageLogDto lastUsage = null;
        for (UsageLog usageLog : usageLogs) {
            String category = categoryMap.get(usageLog.getCategoryId());
            if (category == null) {
                category = "Processing...";
            }
            
            String decryptedApp = EncryptionUtil.decrypt(usageLog.getApp());
            String decryptedTitle = EncryptionUtil.decrypt(usageLog.getTitle());
            String decryptedUrl = EncryptionUtil.decrypt(usageLog.getUrl());
            
            if (lastUsage == null || 
                !lastUsage.title().equals(decryptedTitle) ||
                !lastUsage.app().equals(decryptedApp) ||
                !lastUsage.category().equals(category) ||
                !lastUsage.url().equals(decryptedUrl)
            ) {
                CategorizedUsageLogDto currentUsage = CategorizedUsageLogDto.builder()
                        .app(decryptedApp)
                        .category(category)
                        .title(decryptedTitle)
                        .url(decryptedUrl)
                        .timestamp(TimeZoneUtil.convertUnixToLocalDateTime(usageLog.getTimestamp(), TimeZoneUtil.KOREA_TIMEZONE))
                        .build();
                
                categorizedUsageLogDtos.add(currentUsage);
                lastUsage = currentUsage;
            }
        }
        Collections.reverse(categorizedUsageLogDtos);
        return categorizedUsageLogDtos;
    }

    public List<MergedCategoryUsageLogDto> getMergedCategoryUsageLogByUserId(String userId, LocalDate date) {
        List<MergedCategoryUsageLogDto> mergedCategoryUsageLogDtos = new ArrayList<>();

        DateRange range = getDateRange(date);
        List<UsageLog> usageLogs = usageLogRepository.findUsageLogByUserIdAndTimestampBetween(userId, range.start(), range.end())
                .stream()
                .sorted(Comparator.comparing(UsageLog::getTimestamp))
                .toList();

        Map<ObjectId, String> categoryMap = categoryProvider.getCategoryMap();

        Set<String> workCategories = WorkCategoryType.getAllValues();

        String currentMergedCategory = null;
        LocalDateTime sessionStartTime = null;
        LocalDateTime lastEndTime = null;
        String sessionApp = null;
        String sessionTitle = null;
        
        for (UsageLog usageLog : usageLogs) {
            String mergedCategory = Optional.ofNullable(categoryMap.get(usageLog.getCategoryId()))
                    .map(category -> workCategories.contains(category)? "work":"breaks")
                    .orElse("unknown");

            LocalDateTime usageTime = TimeZoneUtil.convertUnixToLocalDateTime(usageLog.getTimestamp(), TimeZoneUtil.KOREA_TIMEZONE);
            LocalDateTime usageEndTime = usageTime.plusSeconds((long) usageLog.getDuration());

            boolean isNewSession = currentMergedCategory == null ||
                    !currentMergedCategory.equals(mergedCategory) ||
                    (usageTime.isAfter(lastEndTime.plusSeconds(GAP_THRESHOLD)));

            if (isNewSession) {
                if (currentMergedCategory != null) {
                    mergedCategoryUsageLogDtos.add(MergedCategoryUsageLogDto.builder()
                            .mergedCategory(currentMergedCategory)
                            .startedAt(sessionStartTime)
                            .endedAt(lastEndTime)
                            .app(sessionApp)
                            .title(sessionTitle)
                            .build()
                    );
                }
                currentMergedCategory = mergedCategory;
                sessionStartTime = usageTime;
                sessionApp = EncryptionUtil.decrypt(usageLog.getApp());
                sessionTitle = EncryptionUtil.decrypt(usageLog.getTitle());
            }
            lastEndTime = usageEndTime;
        }
        Collections.reverse(mergedCategoryUsageLogDtos);
        return mergedCategoryUsageLogDtos;
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

    private List<SaveUsageLogDto> mergeDuplicatedData(List<SaveUsageLogDto> originalDtos, ObjectId lastUsageLogId) {
        List<SaveUsageLogDto> mergedDtos = new ArrayList<>();

        for (int i=0; i<originalDtos.size()-1; i++) {
            if (isSame(originalDtos.get(i), originalDtos.get(i+1))) {
                originalDtos.set(i+1, SaveUsageLogDto.merge(originalDtos.get(i+1), originalDtos.get(i)));
            } else {
                mergedDtos.add(originalDtos.get(i));
            }
        }
        mergedDtos.add(originalDtos.get(originalDtos.size()-1));

        UsageLog lastSavedUsageLog = null;
        if (lastUsageLogId != null) {
            lastSavedUsageLog = usageLogRepository.findById(lastUsageLogId).orElse(null);
        }
        if (lastSavedUsageLog != null) {
            if (isSame(lastSavedUsageLog, mergedDtos.get(0))) {
                mergedDtos.set(0, SaveUsageLogDto.checkSavedByUsageLogId(mergedDtos.get(0), lastSavedUsageLog.getId()));
            }
        }
        return mergedDtos;
    }

    private boolean isSame(UsageLog original, SaveUsageLogDto updated) {
        String originalApp = EncryptionUtil.decrypt(original.getApp());
        String originalTitle = EncryptionUtil.decrypt(original.getTitle());
        String originalUrl = EncryptionUtil.decrypt(original.getUrl());
        

        boolean contentSame = originalApp.equals(updated.app()) && 
                             originalTitle.equals(updated.title()) && 
                             originalUrl.equals(updated.url());
        double originalEndTime = original.getTimestamp() + original.getDuration();
        double timeDifference = Math.abs(originalEndTime - updated.timestamp());
        boolean timesContinuous = timeDifference <= CONTINUITY_MARGIN_SECONDS;
        
        return contentSame && timesContinuous;
    }
    private boolean isSame(SaveUsageLogDto original, SaveUsageLogDto updated) {
        boolean contentSame = original.app().equals(updated.app()) && 
                             original.title().equals(updated.title()) && 
                             original.url().equals(updated.url());

        double originalEndTime = original.timestamp() + original.duration();
        double timeDifference = Math.abs(originalEndTime - updated.timestamp());
        boolean timesContinuous = timeDifference <= CONTINUITY_MARGIN_SECONDS;
        
        return contentSame && timesContinuous;
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

    private UsageLog toSecuredEntity(UsageLog usageLog) {
        // app, title, url 세 가지만 암호화
        usageLog = UsageLog.builder()
                .id(usageLog.getId())
                .userId(usageLog.getUserId())
                .timestamp(usageLog.getTimestamp())
                .duration(usageLog.getDuration())
                .app(EncryptionUtil.encrypt(usageLog.getApp()))
                .title(EncryptionUtil.encrypt(usageLog.getTitle()))
                .url(EncryptionUtil.encrypt(usageLog.getUrl()))
                .build();
        return usageLog;
    }
}
