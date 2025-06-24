package com.swmStrong.demo.domain.usageLog.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@NoArgsConstructor
@Document(collection = "usage_log")
public class UsageLog {
    @Id
    private ObjectId id;

    private String userId;
    private double timestamp;
    private double duration;
    private String app;
    private String title;
    private String url;
    private ObjectId categoryId;

    @Builder
    public UsageLog(String userId, double timestamp, double duration, String app, String title, String url, ObjectId categoryId) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.duration = duration;
        this.app = app;
        this.title = title;
        this.url = url;
        this.categoryId = categoryId;
    }

    public void updateCategoryId(ObjectId categoryId) {
        this.categoryId = categoryId;
    }
}
