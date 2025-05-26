package com.swmStrong.demo.domain.categoryPattern.dto;

import lombok.Getter;

import java.util.List;

@Getter
public class CategoryPatternJSONDto {
    private List<CategoryPatternEntry> categoryPatterns;

    @Getter
    public static class CategoryPatternEntry {
        private String category;
        private String color;
        private List<String> patterns;
    }
}
