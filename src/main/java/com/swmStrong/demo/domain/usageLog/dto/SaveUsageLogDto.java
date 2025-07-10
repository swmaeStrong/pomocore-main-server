package com.swmStrong.demo.domain.usageLog.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;

public record SaveUsageLogDto(
        String title,
        String app,
        String url,
        double duration,
        double timestamp,
        @JsonIgnore ObjectId usageLogId
) {
    public SaveUsageLogDto(String app, String title, String url, double duration, double timestamp) {
        this(app, title, url, duration, timestamp, null);
    }


    public static SaveUsageLogDto merge(SaveUsageLogDto original, SaveUsageLogDto merged) {
        return new SaveUsageLogDto(
                original.title,
                original.app,
                original.url,
                merged.duration + original.duration,
                merged.timestamp
        );
    }

    public static SaveUsageLogDto checkSavedByUsageLogId(SaveUsageLogDto original, ObjectId usageLogId) {
        return new SaveUsageLogDto(
                original.title,
                original.app,
                original.url,
                original.duration,
                original.timestamp,
                usageLogId
        );
    }
}
