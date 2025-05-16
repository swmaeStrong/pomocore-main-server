package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.config.PatternMatcher;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogRequestDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import com.swmStrong.demo.domain.usageLog.repository.UsageLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsageLogServiceImpl implements UsageLogService {

    private final UsageLogRepository usageLogRepository;
    private final PatternMatcher patternMatcher;

    public UsageLogServiceImpl(
            UsageLogRepository usageLogRepository,
            PatternMatcher patternMatcher

    ) {
        this.usageLogRepository = usageLogRepository;
        this.patternMatcher = patternMatcher;
    }

    @Override
    public void save(SaveUsageLogDto saveUsageLogDto) {
        UsageLog usageLog = UsageLog.builder()
                .userId(saveUsageLogDto.userId())
                .app(saveUsageLogDto.app())
                .title(saveUsageLogDto.title())
                .categories(patternMatcher.search(saveUsageLogDto.title()))
                .duration(saveUsageLogDto.duration())
                .timestamp(saveUsageLogDto.timestamp())
                .build();
        usageLogRepository.save(usageLog);
    }

    @Override
    public List<UsageLogResponseDto> getUsageLogByUserId(String userId) {
        List<UsageLog> usageLogs = usageLogRepository.findByUserId(userId);

        return usageLogs.stream().map(UsageLogResponseDto::from).toList();
    }
}
