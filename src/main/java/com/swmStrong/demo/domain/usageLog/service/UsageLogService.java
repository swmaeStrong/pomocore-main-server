package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.domain.usageLog.dto.CategoryHourlyUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.CategoryUsageDto;
import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;

import java.time.LocalDate;
import java.util.List;

public interface UsageLogService {
    void saveAll(String userId, List<SaveUsageLogDto> saveUsageLogDtoList);
    List<UsageLogResponseDto> getUsageLogByUserId(String userId);
    List<CategoryUsageDto> getUsageLogByUserIdAndDate(String userId, LocalDate date);
    List<CategoryHourlyUsageDto> getUsageLogByUserIdAndDateHourly(String userId, LocalDate date, Integer binSize);
}
