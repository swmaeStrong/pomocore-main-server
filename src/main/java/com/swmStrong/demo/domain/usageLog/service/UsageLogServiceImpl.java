package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.domain.matcher.core.PatternMatcher;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import com.swmStrong.demo.domain.usageLog.repository.UsageLogRepository;
import com.swmStrong.demo.infra.redis.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.StreamConfig;
import com.swmStrong.demo.message.dto.LeaderBoardUsageMessage;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UsageLogServiceImpl implements UsageLogService {

    private final UsageLogRepository usageLogRepository;
    private final PatternMatcher patternMatcher;
    private final RedisStreamProducer redisStreamProducer;

    public UsageLogServiceImpl(
            UsageLogRepository usageLogRepository,
            PatternMatcher patternMatcher,
            RedisStreamProducer redisStreamProducer
    ) {
        this.usageLogRepository = usageLogRepository;
        this.patternMatcher = patternMatcher;
        this.redisStreamProducer = redisStreamProducer;
    }

    //TODO: 문제가 발생할 가능성도 있으니 try - except 구조로 변경
    @Override
    public void saveAll(List<SaveUsageLogDto> saveUsageLogDtoList) {
        for (SaveUsageLogDto saveUsageLogDto : saveUsageLogDtoList) {
            save(saveUsageLogDto);
        }
    }

    private void save(SaveUsageLogDto saveUsageLogDto) {
        UsageLog usageLog = UsageLog.builder()
                .userId(saveUsageLogDto.userId())
                .app(saveUsageLogDto.app())
                .title(saveUsageLogDto.title())
                .categories(patternMatcher.search(saveUsageLogDto.title()))
                .duration(saveUsageLogDto.duration())
                .timestamp(saveUsageLogDto.timestamp())
                .build();

        usageLogRepository.save(usageLog);

        redisStreamProducer.send(
                StreamConfig.LEADERBOARD.getStreamKey(),
                LeaderBoardUsageMessage.builder()
                        .userId(usageLog.getUserId())
                        .category(usageLog.getCategories().iterator().next())
                        .duration(usageLog.getDuration())
                        .timestamp(usageLog.getTimestamp())
                        .build()
        );
    }

    @Override
    public List<UsageLogResponseDto> getUsageLogByUserId(String userId) {
        List<UsageLog> usageLogs = usageLogRepository.findByUserId(userId);

        return usageLogs.stream().map(UsageLogResponseDto::from).toList();
    }

    @Override
    public List<CategoryUsageDto> getUsageLogByUserIdToday(String userId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return usageLogRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }
}
