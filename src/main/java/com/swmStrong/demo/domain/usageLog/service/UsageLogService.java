package com.swmStrong.demo.domain.usageLog.service;

import com.swmStrong.demo.domain.usageLog.dto.SaveUsageLogDto;
import com.swmStrong.demo.domain.usageLog.dto.UsageLogResponseDto;

import java.util.List;

public interface UsageLogService {
    void saveAll(List<SaveUsageLogDto> saveUsageLogDtoList);
    List<UsageLogResponseDto> getUsageLogByUserId(String userId);
}
