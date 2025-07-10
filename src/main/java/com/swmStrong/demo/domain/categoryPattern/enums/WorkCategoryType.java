package com.swmStrong.demo.domain.categoryPattern.enums;

import java.util.HashSet;
import java.util.Set;

public enum WorkCategoryType {
    DEVELOPMENT("Development"),
    LLM("LLM"),
    DOCUMENTATION("Documentation"),
    DESIGN("Design"),
    VIDEO_EDITING("Video Editing"),
    EDUCATION("Education"),
    PRODUCTIVITY("Productivity"),;

    private final String category;

    WorkCategoryType(String category) {
        this.category = category;
    }

    public static Set<String> getAllValues() {
        HashSet<String> result = new HashSet<>();
        for (WorkCategoryType type : WorkCategoryType.values()) {
            result.add(type.category);
        }
        return result;
    }
}
