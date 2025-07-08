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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Base64;

@Slf4j
@Service
public class UsageLogServiceImpl implements UsageLogService {

    private static final String USAGE_LOG_LAST_TIMESTAMP_PREFIX = "usageLog:lastTimestamp:";
    private static final long CACHE_EXPIRE_SECONDS = 86400; // 24 hours
    private static final double TIME_TOLERANCE_SECONDS = 60.0;
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

            List<UsageLog> securedUsageLogs = usageLogs.stream()
                    .map(this::toSecuredEntity)
                    .toList();

            usageLogRepository.saveAll(securedUsageLogs);

            usageLogs.stream()
                    .map(UsageLog::getTimestamp)
                    .max(Double::compareTo)
                    .ifPresent(maxTimestamp -> 
                        redisRepository.setDataWithExpire(cacheKey, maxTimestamp.toString(), CACHE_EXPIRE_SECONDS)
                    );

            for (int i = 0; i < securedUsageLogs.size(); i++) {
                UsageLog securedUsageLog = securedUsageLogs.get(i);
                UsageLog originalUsageLog = usageLogs.get(i);
                redisStreamProducer.send(
                        StreamConfig.PATTERN_MATCH.getStreamKey(),
                        PatternClassifyMessage.builder()
                                .usageLogId(securedUsageLog.getId().toHexString())
                                .app(originalUsageLog.getApp())
                                .title(originalUsageLog.getTitle())
                                .url(originalUsageLog.getUrl())
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
                .id(null)
                .userId(usageLog.getUserId())
                .timestamp(usageLog.getTimestamp())
                .duration(usageLog.getDuration())
                .app(EncryptionUtil.encrypt(usageLog.getApp()))
                .title(EncryptionUtil.encrypt(usageLog.getTitle()))
                .url(EncryptionUtil.encrypt(usageLog.getUrl()))
                .build();
        return usageLog;
    }

    @Override
    @Async("asyncExecutor")
    public void encryptExistingData() {
        log.info("Starting encryption/re-encryption of existing data...");

        List<UsageLog> allUsageLogs = usageLogRepository.findAll();
        log.info("Found {} usage logs to process", allUsageLogs.size());

        int newlyEncryptedCount = 0;
        int reencryptedCount = 0;
        int alreadyEncryptedCount = 0;
        int batchSize = 100;
        List<UsageLog> batchToSave = new ArrayList<>();

        for (int i = allUsageLogs.size() - 1; i >= 0; i--) {
            UsageLog usageLog = allUsageLogs.get(i);
            try {
                EncryptionStatus status = getEncryptionStatus(usageLog);
                
                UsageLog processedUsageLog = null;
                
                switch (status) {
                    case ENCRYPTED_WITH_NEW_KEY:
                        alreadyEncryptedCount++;
                        continue; // Skip - already encrypted with new key
                        
                    case ENCRYPTED_WITH_LEGACY_KEY:
                        // Re-encrypt with new key
                        processedUsageLog = UsageLog.builder()
                                .id(usageLog.getId()) // 기존 ID 유지
                                .userId(usageLog.getUserId())
                                .timestamp(usageLog.getTimestamp())
                                .duration(usageLog.getDuration())
                                .categoryId(usageLog.getCategoryId())
                                .app(EncryptionUtil.reencryptIfNeeded(usageLog.getApp()))
                                .title(EncryptionUtil.reencryptIfNeeded(usageLog.getTitle()))
                                .url(EncryptionUtil.reencryptIfNeeded(usageLog.getUrl()))
                                .build();
                        reencryptedCount++;
                        break;
                        
                    case NOT_ENCRYPTED:
                        // Encrypt with new key
                        processedUsageLog = UsageLog.builder()
                                .id(usageLog.getId()) // 기존 ID 유지
                                .userId(usageLog.getUserId())
                                .timestamp(usageLog.getTimestamp())
                                .duration(usageLog.getDuration())
                                .categoryId(usageLog.getCategoryId())
                                .app(EncryptionUtil.encrypt(usageLog.getApp()))
                                .title(EncryptionUtil.encrypt(usageLog.getTitle()))
                                .url(EncryptionUtil.encrypt(usageLog.getUrl()))
                                .build();
                        newlyEncryptedCount++;
                        break;
                }

                if (processedUsageLog != null) {
                    batchToSave.add(processedUsageLog);
                }

                if (batchToSave.size() >= batchSize) {
                    usageLogRepository.saveAll(batchToSave);
                    log.info("Processed {} records so far... (New: {}, Re-encrypted: {}, Already encrypted: {})",
                            newlyEncryptedCount + reencryptedCount + alreadyEncryptedCount,
                            newlyEncryptedCount, reencryptedCount, alreadyEncryptedCount);
                    batchToSave.clear();
                }
            } catch (Exception e) {
                log.error("Failed to process usage log with ID: {}, error: {}",
                        usageLog.getId(), e.getMessage());
            }
        }

        if (!batchToSave.isEmpty()) {
            usageLogRepository.saveAll(batchToSave);
        }

        log.info("Encryption completed. Newly encrypted: {}, Re-encrypted: {}, Already encrypted: {}, Total: {}",
                newlyEncryptedCount, reencryptedCount, alreadyEncryptedCount, allUsageLogs.size());
    }

    private enum EncryptionStatus {
        ENCRYPTED_WITH_NEW_KEY,
        ENCRYPTED_WITH_LEGACY_KEY,
        NOT_ENCRYPTED
    }

    private EncryptionStatus getEncryptionStatus(UsageLog usageLog) {
        try {
            // Try to decrypt with new key first
            byte[] appBytes = Base64.getDecoder().decode(usageLog.getApp());
            byte[] titleBytes = Base64.getDecoder().decode(usageLog.getTitle());
            byte[] urlBytes = Base64.getDecoder().decode(usageLog.getUrl());
            
            // Check if data has minimum length for encrypted data
            if (appBytes.length < 28 || titleBytes.length < 28 || urlBytes.length < 28) {
                return EncryptionStatus.NOT_ENCRYPTED;
            }
            
            // Try to decrypt with new SHA256 key
            try {
                EncryptionUtil.decrypt(usageLog.getApp());
                EncryptionUtil.decrypt(usageLog.getTitle());
                EncryptionUtil.decrypt(usageLog.getUrl());
                return EncryptionStatus.ENCRYPTED_WITH_NEW_KEY;
            } catch (Exception e) {
                // If new key fails, it might be encrypted with legacy key
                return EncryptionStatus.ENCRYPTED_WITH_LEGACY_KEY;
            }
        } catch (Exception e) {
            // If base64 decode fails, data is not encrypted
            return EncryptionStatus.NOT_ENCRYPTED;
        }
    }

    private boolean isAlreadyEncrypted(UsageLog usageLog) {
        return getEncryptionStatus(usageLog) != EncryptionStatus.NOT_ENCRYPTED;
    }
}
