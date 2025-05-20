package com.swmStrong.demo.domain.categoryPattern.repository;


public interface CustomCategoryPatternRepository {
    void addPattern(String category, String newPattern);
    void removePattern(String category, String pattern);
}
