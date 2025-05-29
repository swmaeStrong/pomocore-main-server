package com.swmStrong.demo.domain.categoryPattern.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Comparator;
import java.util.Set;

@Document(collection = "category_pattern")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CategoryPattern implements Comparable<CategoryPattern> {
    @Id
    private ObjectId id;

    private String category;
    private Set<String> patterns;
    private Integer priority;
    private String color;

    public void setCategory(String category) {
        this.category = category;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Builder
    public CategoryPattern(String category, Set<String> patterns, Integer priority, String color) {
        this.category = category;
        this.patterns = patterns;
        this.priority = priority;
        this.color = color;
    }

    @Override
    public int compareTo(CategoryPattern o) {
        return Comparator
                .comparing(CategoryPattern::getPriority, Comparator.nullsLast(Integer::compareTo))
                .compare(this, o);
    }
}