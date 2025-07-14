package com.swmStrong.demo.domain.pomodoro.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;


@Getter
@Document(collection = "pomodoro_usage_log")
public class PomodoroUsageLog {

    @Id
    private ObjectId id;

    private int session;

    private int sessionMinutes;

    private LocalDate sessionDate;

    private String userId;

    private ObjectId categoryId;

    private String app;

    private double timestamp;

    private double duration;

    @Builder
    public PomodoroUsageLog(String userId, int session, int sessionMinutes, LocalDate sessionDate, ObjectId categoryId, String app, double timestamp, double duration) {
        this.session = session;
        this.sessionMinutes = sessionMinutes;
        this.sessionDate = sessionDate;
        this.userId = userId;
        this.categoryId = categoryId;
        this.app = app;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public void updateCategoryId(ObjectId categoryId) {
        this.categoryId = categoryId;
    }
}
