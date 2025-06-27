package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.domain.usageLog.dto.*;

import java.time.LocalDate;
import java.util.List;

public interface UsageLogService {
    void saveAll(String userId, List<SaveUsageLogDto> saveUsageLogDtoList);
    List<CategorizedUsageLogDto> getCategorizedUsageLogByUserId(String userId);
    List<CategoryUsageDto> getUsageLogByUserIdAndDate(String userId, LocalDate date);
    List<CategoryHourlyUsageDto> getUsageLogByUserIdAndDateHourly(String userId, LocalDate date, Integer binSize);
}
