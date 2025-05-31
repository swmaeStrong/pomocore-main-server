package com.swmStrong.demo.domain.portone.dto;

import java.util.List;

public class ScheduledPaymentResult {
    private final boolean success;
    private final List<String> scheduledIds;
    private final String errorType;

    private ScheduledPaymentResult(boolean success, List<String> scheduledIds, String errorType) {
        this.success = success;
        this.scheduledIds = scheduledIds;
        this.errorType = errorType;
    }

    public static ScheduledPaymentResult of(boolean success, List<String> scheduledIds, String errorType) {
        return new ScheduledPaymentResult(success, scheduledIds, errorType);
    }

    public boolean isSuccess() {
        return success;
    }

    public List<String> getScheduledIds() {
        return scheduledIds;
    }

    public String getErrorType() {
        return errorType;
    }
}
