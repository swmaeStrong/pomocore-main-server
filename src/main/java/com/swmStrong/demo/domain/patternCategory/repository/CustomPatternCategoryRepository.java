package com.swmStrong.demo.domain.patternCategory.repository;


public interface CustomPatternCategoryRepository {
    void addPattern(String category, String newPattern);
    void removePattern(String category, String pattern);
}
