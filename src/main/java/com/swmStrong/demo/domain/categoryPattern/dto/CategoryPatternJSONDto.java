package com.swmStrong.demo.domain.categoryPattern.dto;

import lombok.Getter;

import java.util.List;
import java.util.Set;

@Getter
public class CategoryPatternJSONDto {
    private List<CategoryPatternEntry> categoryPatterns;

    @Getter
    public static class CategoryPatternEntry {
        private String category;
        private Set<String> appPatterns;
        private Set<String> domainPatterns;
    }
}
