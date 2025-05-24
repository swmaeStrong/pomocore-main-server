package com.swmStrong.demo.domain.categoryPattern.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document(collection = "category_pattern")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryPattern {
    @Id
    private String id;

    private String category;
    private Set<String> patterns;
    private String color;

    public void setCategory(String category) {
        this.category = category;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Builder
    public CategoryPattern(String id, String category, Set<String> patterns, String color) {
        this.id = id;
        this.category = category;
        this.patterns = patterns;
        this.color = color;
    }
}