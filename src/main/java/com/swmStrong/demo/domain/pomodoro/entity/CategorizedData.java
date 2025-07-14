package com.swmStrong.demo.domain.pomodoro.entity;

import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "categorized_data")
public class CategorizedData {
    @Id
    private ObjectId id;

    private String app;

    private String url;

    private String title;

    private ObjectId categoryId;

    @Builder
    public CategorizedData(String app, String url, String title, ObjectId categoryId) {
        this.app = app;
        this.url = url;
        this.title = title;
        this.categoryId = categoryId;
    }

    public void updateCategoryId(ObjectId categoryId) {
        this.categoryId = categoryId;
    }
}
