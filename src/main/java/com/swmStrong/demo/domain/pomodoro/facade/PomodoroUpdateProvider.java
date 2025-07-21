package com.swmStrong.demo.domain.pomodoro.facade;

import com.swmStrong.demo.common.exception.ApiException;
import com.swmStrong.demo.common.exception.code.ErrorCode;
import com.swmStrong.demo.domain.pomodoro.entity.CategorizedData;
import com.swmStrong.demo.domain.pomodoro.entity.PomodoroUsageLog;
import com.swmStrong.demo.domain.pomodoro.repository.CategorizedDataRepository;
import com.swmStrong.demo.domain.pomodoro.repository.PomodoroUsageLogRepository;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

@Service
public class PomodoroUpdateProvider {

    private final CategorizedDataRepository categorizedDataRepository;
    private final PomodoroUsageLogRepository pomodoroUsageLogRepository;

    public PomodoroUpdateProvider(
            CategorizedDataRepository categorizedDataRepository,
            PomodoroUsageLogRepository pomodoroUsageLogRepository
    ) {
        this.categorizedDataRepository = categorizedDataRepository;
        this.pomodoroUsageLogRepository = pomodoroUsageLogRepository;
    }

    public void updatePomodoroUsageLogByCategoryId(ObjectId pomodoroUsageLogId, ObjectId categoryId) {
        PomodoroUsageLog pomodoroUsageLog = pomodoroUsageLogRepository.findById(pomodoroUsageLogId)
                .orElseThrow(() -> new ApiException(ErrorCode.USAGE_LOG_NOT_FOUND));

        pomodoroUsageLog.updateCategoryId(categoryId);
        pomodoroUsageLogRepository.save(pomodoroUsageLog);
    }


    public void updateCategorizedDataByCategoryId(ObjectId categorizedDataId, ObjectId categoryId, boolean isLLMBased) {
        CategorizedData categorizedData = categorizedDataRepository.findById(categorizedDataId)
                .orElseThrow(() -> new ApiException(ErrorCode.USAGE_LOG_NOT_FOUND));

        categorizedData.updateCategoryId(categoryId);
        categorizedData.checkLLMBased(isLLMBased);
        categorizedDataRepository.save(categorizedData);
    }
}
