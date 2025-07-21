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

    private String userId;

    private ObjectId CategorizedDataId;

    private ObjectId categoryId;

    private int session;

    private int sessionMinutes;

    private LocalDate sessionDate;

    private double timestamp;

    private double duration;

    @Builder
    public PomodoroUsageLog(
            String userId, ObjectId CategorizedDataId, int session, int sessionMinutes,
            LocalDate sessionDate, ObjectId categoryId, double timestamp, double duration
    ) {
        this.userId = userId;
        this.CategorizedDataId = CategorizedDataId;
        this.session = session;
        this.sessionMinutes = sessionMinutes;
        this.sessionDate = sessionDate;
        this.categoryId = categoryId;
        this.timestamp = timestamp;
        this.duration = duration;
    }

    public void updateCategoryId(ObjectId categoryId) {
        this.categoryId = categoryId;
    }

    public void updateCategorizedDataId(ObjectId categorizedDataId) {
        this.CategorizedDataId = categorizedDataId;
    }
}
