package com.swmStrong.demo.domain.usageLog.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@NoArgsConstructor
@Document(collection = "usage_logs")
public class UsageLog {
    @Id
    private String id;

    private String userId;
    private LocalDateTime timestamp;
    private long duration;
    private String app;
    private String title;
    private Set<String> categories;

    @Builder
    public UsageLog(String userId, LocalDateTime timestamp, long duration, String app, String title, Set<String> categories) {
        this.userId = userId;
        this.timestamp = timestamp;
        this.duration = duration;
        this.app = app;
        this.title = title;
        this.categories = categories;
    }
}
