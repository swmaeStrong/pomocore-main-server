package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.domain.categoryPattern.facade.CategoryProvider;
import com.swmStrong.demo.domain.matcher.core.PatternMatcher;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import com.swmStrong.demo.domain.usageLog.repository.UsageLogRepository;
import com.swmStrong.demo.infra.redis.RedisStreamProducer;
import com.swmStrong.demo.infra.redis.StreamConfig;
import com.swmStrong.demo.message.dto.LeaderBoardUsageMessage;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class UsageLogServiceImpl implements UsageLogService {

    private final UsageLogRepository usageLogRepository;
    private final PatternMatcher patternMatcher;
    private final RedisStreamProducer redisStreamProducer;
    private final CategoryProvider categoryProvider;

    public UsageLogServiceImpl(
            UsageLogRepository usageLogRepository,
            PatternMatcher patternMatcher,
            RedisStreamProducer redisStreamProducer,
            CategoryProvider categoryProvider
    ) {
        this.usageLogRepository = usageLogRepository;
        this.patternMatcher = patternMatcher;
        this.redisStreamProducer = redisStreamProducer;
        this.categoryProvider = categoryProvider;
    }

    //TODO: 문제가 발생할 가능성도 있으니 try - except 구조로 변경
    @Override
    public void saveAll(List<SaveUsageLogDto> saveUsageLogDtoList) {
        for (SaveUsageLogDto saveUsageLogDto : saveUsageLogDtoList) {
            save(saveUsageLogDto);
        }
    }


    private void save(SaveUsageLogDto saveUsageLogDto) {
        //TODO: 현재 데이터 라벨링 관련 논의 시 app이 우선권을 가짐. 이후 변경되는 점 업데이트 필요
        Set<ObjectId> categories = patternMatcher.search(saveUsageLogDto.app());
        if (categories.isEmpty()) {
            categories.addAll(patternMatcher.search(saveUsageLogDto.title()));
        }

        if (categories.isEmpty()) {
            categories.add(categoryProvider.getCategoryIdByCategory("Uncategorized"));
        }

        UsageLog usageLog = UsageLog.builder()
                .userId(saveUsageLogDto.userId())
                .app(saveUsageLogDto.app())
                .title(saveUsageLogDto.title())
                .categories(categories)
                .duration(saveUsageLogDto.duration())
                .timestamp(saveUsageLogDto.timestamp())
                .build();

        usageLogRepository.save(usageLog);

        redisStreamProducer.send(
                StreamConfig.LEADERBOARD.getStreamKey(),
                LeaderBoardUsageMessage.builder()
                        .userId(usageLog.getUserId())
                        .categoryId(usageLog.getCategories().iterator().next())
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
    public List<CategoryUsageDto> getUsageLogByUserIdAndDate(String userId, LocalDate date) {
        if (date == null) {
            date = LocalDate.now();
        }

        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = start.plusDays(1);
        return usageLogRepository.findByUserIdAndTimestampBetween(userId, start, end);
    }
}
