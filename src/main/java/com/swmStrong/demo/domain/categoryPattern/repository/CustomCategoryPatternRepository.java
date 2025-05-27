package com.swmStrong.demo.domain.categoryPattern.repository;


import java.util.Set;

public interface CustomCategoryPatternRepository {
    void addPattern(String category, String newPattern);
    void removePattern(String category, String pattern);
    Set<String> findPatternsByCategory(String category);
}
