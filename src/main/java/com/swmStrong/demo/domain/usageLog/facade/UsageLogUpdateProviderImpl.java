package com.swmStrong.demo.domain.usageLog.facade;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import com.swmStrong.demo.domain.usageLog.repository.UsageLogRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class UsageLogUpdateProviderImpl implements UsageLogUpdateProvider {

    private final UsageLogRepository usageLogRepository;

    public UsageLogUpdateProviderImpl(UsageLogRepository usageLogRepository) {
        this.usageLogRepository = usageLogRepository;
    }

    @Override
    public UsageLog updateCategory(ObjectId usageLogId, ObjectId categoryPatternId) {
        UsageLog usageLog = usageLogRepository.findById(usageLogId)
                .orElseThrow(() -> new ApiException(ErrorCode.USAGE_LOG_NOT_FOUND));

        usageLog.updateCategoryId(categoryPatternId);
        return usageLogRepository.save(usageLog);
    }

    @Override
    public UsageLog loadByUsageLogId(ObjectId usageLogId) {
        return usageLogRepository.findById(usageLogId)
                .orElseThrow(() -> new ApiException(ErrorCode.USAGE_LOG_NOT_FOUND));
    }
}
