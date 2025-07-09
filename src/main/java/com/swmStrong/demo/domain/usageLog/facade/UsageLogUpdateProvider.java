package com.swmStrong.demo.domain.usageLog.facade;

import com.swmStrong.demo.domain.usageLog.entity.UsageLog;
import org.bson.types.ObjectId;

public interface UsageLogUpdateProvider {
    UsageLog updateCategory(ObjectId usageLogId, ObjectId categoryPatternId);
    UsageLog loadByUsageLogId(ObjectId usageLogId);
}
